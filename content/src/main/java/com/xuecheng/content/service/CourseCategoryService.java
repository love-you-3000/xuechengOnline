package com.xuecheng.content.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.CourseCategoryTreeDto;
import com.xuecheng.content.entity.CourseCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-06-03
 */
public interface CourseCategoryService extends IService<CourseCategory> {
    List<CourseCategoryTreeDto> queryCategory();
}
