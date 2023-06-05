package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.AddCourseDto;
import com.xuecheng.content.dto.CourseBaseInfoDto;
import com.xuecheng.content.dto.QueryCourseParamsDto;
import com.xuecheng.content.entity.CourseBase;


/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-06-03
 */
public interface CourseBaseService extends IService<CourseBase> {
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

}
