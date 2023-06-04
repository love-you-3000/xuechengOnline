package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.CourseCategoryTreeDto;
import com.xuecheng.content.entity.CourseCategory;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.service.CourseCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryCategory() {
        ArrayList<CourseCategoryTreeDto> item = new ArrayList<>();
        LambdaQueryWrapper<CourseCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseCategory::getParentid, 1);
        List<CourseCategory> list = courseCategoryMapper.selectList(wrapper);
        for (CourseCategory courseCategory : list) {
            CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(courseCategory,courseCategoryTreeDto);
            LambdaQueryWrapper<CourseCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseCategory::getParentid,courseCategory.getParentid());
            courseCategoryTreeDto.setChildrenTreeNodes(courseCategoryMapper.selectList(queryWrapper));
            item.add(courseCategoryTreeDto);
        }
        return item;

    }
}
