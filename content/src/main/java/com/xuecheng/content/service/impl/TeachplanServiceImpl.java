package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        List<Teachplan> teachplans = teachplanMapper.selectList(wrapper); // 该课程的所有父课程
        if (teachplans != null) {
            List<TeachplanDto> res = teachplans.stream().map((item -> {
                TeachplanDto dto = new TeachplanDto();
                BeanUtils.copyProperties(item, dto);
                LambdaQueryWrapper<TeachplanMedia> mediaWrapper = new LambdaQueryWrapper<>();
                mediaWrapper.eq(TeachplanMedia::getTeachplanId, item.getId());
                TeachplanMedia teachplanMedia = mediaMapper.selectOne(mediaWrapper);
                dto.setTeachplanMedia(teachplanMedia);
                LambdaQueryWrapper<Teachplan> teachplanWrapper = new LambdaQueryWrapper<>();
                teachplanWrapper.eq(Teachplan::getParentid, item.getId());
                List<Teachplan> list = teachplanMapper.selectList(teachplanWrapper);
                dto.setTeachPlanTreeNodes(list.stream().map(plan -> {
                    TeachplanDto dto1 = new TeachplanDto();
                    BeanUtils.copyProperties(plan, dto1);
                    LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, dto1.getId());
                    dto1.setTeachplanMedia(mediaMapper.selectOne(mediaLambdaQueryWrapper));
                    return dto1;
                }).collect(Collectors.toList()));
                return dto;
            })).collect(Collectors.toList());
            return res;
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
}
