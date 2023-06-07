package com.xuecheng.content.controller;

import com.xuecheng.content.entity.CourseTeacher;
import org.springframework.web.bind.annotation.*;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService  courseTeacherService;

    @ApiOperation("获取教师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getTeacherInfo(@PathVariable Long courseId){
        return courseTeacherService.getTeacherInfo(courseId);
    }

    @ApiOperation("新增老师信息")
    @PostMapping("courseTeacher")
    public CourseTeacher saveTeacher(@RequestBody  CourseTeacher courseTeacher)
    {
        return courseTeacherService.saveTeacher(courseTeacher);
    }

}
