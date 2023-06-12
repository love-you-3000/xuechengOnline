package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.content.dto.BindTeachplanMediaDto;
import com.xuecheng.content.dto.TeachplanDto;
import com.xuecheng.content.entity.Teachplan;
import com.xuecheng.content.entity.TeachplanMedia;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper mediaMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, courseId);
        wrapper.eq(Teachplan::getParentid, 0);
        List<Teachplan> teachPlan = teachplanMapper.selectList(wrapper); // 该课程的所有父课程
        if (teachPlan != null) {
            return teachPlan.stream().map((item -> {
                TeachplanDto dto = new TeachplanDto();
                BeanUtils.copyProperties(item, dto);
                LambdaQueryWrapper<TeachplanMedia> mediaWrapper = new LambdaQueryWrapper<>();
                mediaWrapper.eq(TeachplanMedia::getTeachplanId, item.getId());
                TeachplanMedia teachplanMedia = mediaMapper.selectOne(mediaWrapper);
                dto.setTeachplanMedia(teachplanMedia);
                LambdaQueryWrapper<Teachplan> teachPlanWrapper = new LambdaQueryWrapper<>();
                teachPlanWrapper.eq(Teachplan::getParentid, item.getId());
                List<Teachplan> list = teachplanMapper.selectList(teachPlanWrapper);
                dto.setTeachPlanTreeNodes(list.stream().map(plan -> {
                    TeachplanDto dto1 = new TeachplanDto();
                    BeanUtils.copyProperties(plan, dto1);
                    LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, dto1.getId());
                    dto1.setTeachplanMedia(mediaMapper.selectOne(mediaLambdaQueryWrapper));
                    return dto1;
                }).sorted(Comparator.comparingInt(Teachplan::getOrderby)).collect(Collectors.toList()));
                return dto;
            })).sorted(Comparator.comparingInt(Teachplan::getOrderby)).collect(Collectors.toList());
        }
        return null;
    }

    @Transactional
    @Override
    public void saveTeachplan(Teachplan teachplan) {
        if (teachplan.getId() == null) {
            //新增
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid, teachplan.getParentid());
            wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            Integer integer = teachplanMapper.selectCount(wrapper);
            teachplan.setOrderby(integer + 1);
            teachplanMapper.insert(teachplan);
        }
        teachplanMapper.updateById(teachplan);
    }

    @Transactional
    @Override
    public void move(Long teachPlanId, boolean up) {
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null) throw new XuechengException("操作失败！");
        // 下移大章节
        // 获取大章节总数
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        Integer count = teachplanMapper.selectCount(wrapper);
        // 如果当前序号等于总数，说明已经到底，无需下移
        //
        // 否则，和比自己序号小1的课程交换排序大小
        if (up && teachplan.getOrderby() > 1 || !up && teachplan.getOrderby() < count) {

            teachplan.setOrderby(up ? teachplan.getOrderby() - 1 : teachplan.getOrderby() + 1);
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid, teachplan.getParentid());
            wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            wrapper.eq(Teachplan::getOrderby, teachplan.getOrderby());
            Teachplan temp = teachplanMapper.selectOne(wrapper);
            temp.setOrderby(up ? teachplan.getOrderby() + 1 : teachplan.getOrderby() - 1);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(temp);
        }
    }

    @Transactional
    @Override
    public void deleteTeachPlan(Long teachPlanId) {
        // 删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan.getParentid() == 0) {
            // 删除大章节
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            wrapper.eq(Teachplan::getParentid, teachplan.getId());
            Integer count = teachplanMapper.selectCount(wrapper);
            if (count != 0) throw new XuechengException("课程计划信息还有子级信息，无法操作");
            teachplanMapper.deleteById(teachPlanId);
        }
        //删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
        else {
            LambdaQueryWrapper<TeachplanMedia> mediaWrapper = new LambdaQueryWrapper<>();
            mediaWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
            mediaWrapper.eq(TeachplanMedia::getCourseId, teachplan.getCourseId());
            teachplanMapper.deleteById(teachPlanId);
            mediaMapper.delete(mediaWrapper);
        }
        changeOrderBy(teachplan.getCourseId(), teachplan.getParentid(), teachplan.getOrderby());
    }

    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XuechengException.cast("课程计划不存在！");
        }
        if (teachplan.getGrade() == null || teachplan.getGrade() != 2) {
            XuechengException.cast("只允许第二级教学计划绑定媒资文件！");
        }
        Long courseId = teachplan.getCourseId();
        // 先删除原有的绑定关系
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public void DeleteMedia(Long teachplanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
        wrapper.eq(TeachplanMedia::getMediaId,mediaId);
        int delete = teachplanMediaMapper.delete(wrapper);
        if(delete<=0) XuechengException.cast("删除绑定失败！");
    }

    private void changeOrderBy(Long courseId, Long parentId, Integer orderBy) {
        // 所有同级课程，序号比orderBy大的全部减1
        // todo 该逻辑对数据库操作太频繁，后期估计要调整排序逻辑
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, courseId);
        wrapper.eq(Teachplan::getParentid, parentId);
        wrapper.gt(Teachplan::getOrderby, orderBy);
        List<Teachplan> list = teachplanMapper.selectList(wrapper);
        list.forEach(item -> item.setOrderby(item.getOrderby() - 1));
        for (Teachplan teachplan : list) {
            teachplanMapper.updateById(teachplan);
        }
    }
}
