package com.xuecheng.content.controller;

import com.xuecheng.content.dto.TeachplanDto;
import com.xuecheng.content.entity.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程计划 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;


    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程Id", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.getTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody Teachplan teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("下移课程计划")
    @PostMapping("/teachplan/movedown/{teachPlanId}")
    public void moveDown(@PathVariable Long teachPlanId) {
        teachplanService.move(teachPlanId, false);
    }

    @ApiOperation("上移课程计划")
    @PostMapping("/teachplan/moveup/{teachPlanId}")
    public void moveUp(@PathVariable Long teachPlanId) {
        teachplanService.move(teachPlanId, true);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("teachplan/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId) {
        teachplanService.deleteTeachPlan(teachPlanId);
    }
}
