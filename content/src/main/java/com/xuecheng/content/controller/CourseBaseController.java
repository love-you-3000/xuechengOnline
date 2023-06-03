package com.xuecheng.content.controller;

import com.xuecheng.content.dto.QueryCourseParamsDto;
import com.xuecheng.content.entity.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 课程基本信息 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping()
public class CourseBaseController {


    @Autowired
    private CourseBaseService courseBaseService;

    @ApiOperation(value = "课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }
}
