package com.xuecheng.learning.service;

import com.xuecheng.learning.dto.XcChooseCourseDto;
import com.xuecheng.learning.dto.XcCourseTablesDto;

/**
 * @className: MyCourseTablesService
 * @author: 朱江
 * @description:
 * @date: 2023/6/21
 **/
public interface MyCourseTablesService {

    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
