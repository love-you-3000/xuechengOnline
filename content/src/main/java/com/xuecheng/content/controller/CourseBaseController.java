package com.xuecheng.content.controller;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.AddCourseDto;
import com.xuecheng.content.dto.CourseBaseInfoDto;
import com.xuecheng.content.dto.EditCourseDto;
import com.xuecheng.content.dto.QueryCourseParamsDto;
import com.xuecheng.content.entity.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 课程基本信息 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RequestMapping()
public class CourseBaseController {

    @Autowired
    private CourseBaseService courseBaseService;

    @ApiOperation(value = "课程查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) throw new RuntimeException("获取用户失败，请先登录！");
        String companyId = user.getCompanyId();
        return courseBaseService.queryCourseBaseList(Long.parseLong(companyId), pageParams, queryCourseParamsDto);
    }

    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("获取课程基本信息")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable(value = "id") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user != null) {
            System.out.println(user.toString());
        }
        return courseBaseService.getCourseBaseInfoDto(courseId);
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        Long companyId = 1232141425L;
        return courseBaseService.updateCourseBase(companyId, editCourseDto);
    }


    @ApiOperation("删除课程信息")
    @DeleteMapping("/course/{courseId}")
    public void modifyCourseBase(@PathVariable Long courseId) {
        courseBaseService.deleteCourse(courseId);
    }

}
