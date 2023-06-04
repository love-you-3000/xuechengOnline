package com.xuecheng.content.controller;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.CourseCategoryTreeDto;
import com.xuecheng.content.entity.CourseCategory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 课程分类 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@Api(value = "课程分类信息查询接口",tags = "课程分类信息查询接口")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService  courseCategoryService;

    @ApiOperation(value = "分类树查询接口")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> getCategory()
    {
        return courseCategoryService.queryCategory();
    }
}
