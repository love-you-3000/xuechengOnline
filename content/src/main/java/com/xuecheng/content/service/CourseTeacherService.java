package com.xuecheng.content.service;

import com.xuecheng.content.entity.CourseTeacher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-06-03
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> getTeacherInfo(Long courseId);

    CourseTeacher saveTeacher(CourseTeacher courseTeacher);
}
