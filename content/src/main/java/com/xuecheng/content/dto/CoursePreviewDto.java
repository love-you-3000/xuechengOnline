package com.xuecheng.content.dto;

import com.xuecheng.content.entity.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * @className: CoursePreviewDto
 * @author: 朱江
 * @description: 用于课程预览的模型类
 * @date: 2023/6/13
 **/

@Data
public class CoursePreviewDto {

    // 课程基本信息、营销信息
    CourseBaseInfoDto courseBase;

    // 课程计划信息
    List<TeachplanDto> teachPlans;

    // Todo 师资信息
    List<CourseTeacher> teacher;
}
