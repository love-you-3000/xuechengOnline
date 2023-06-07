package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.entity.CourseTeacher;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper teacherMapper;

    @Override
    public List<CourseTeacher> getTeacherInfo(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId, courseId);
        CourseTeacher courseTeacher = teacherMapper.selectOne(wrapper);
        ArrayList<CourseTeacher> list = new ArrayList<>();
        if(courseTeacher!=null) list.add(courseTeacher);
        return list.size()==0?null:list;
    }

    @Override
    public CourseTeacher saveTeacher(CourseTeacher courseTeacher) {
        Long teacherId = courseTeacher.getId();
        if(teacherId==null)
            teacherMapper.insert(courseTeacher);
        else
            teacherMapper.updateById(courseTeacher);
        return courseTeacher;
    }
}
