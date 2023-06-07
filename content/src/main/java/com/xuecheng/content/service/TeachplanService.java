package com.xuecheng.content.service;

import com.xuecheng.content.dto.TeachplanDto;
import com.xuecheng.content.entity.Teachplan;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-06-03
 */
public interface TeachplanService extends IService<Teachplan> {

    List<TeachplanDto> getTreeNodes(Long courseId);

    void saveTeachplan(Teachplan saveTeachplanDto);

    void move(Long teachPlanId, boolean up);

    void deleteTeachPlan(Long teachPlanId);
}

