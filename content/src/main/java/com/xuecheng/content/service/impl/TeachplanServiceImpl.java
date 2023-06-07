package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XuechengException;
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
}
