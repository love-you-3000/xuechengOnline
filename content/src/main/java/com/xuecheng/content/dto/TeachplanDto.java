package com.xuecheng.content.dto;

import com.xuecheng.content.entity.Teachplan;
import com.xuecheng.content.entity.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @className: TeachplanDto
 * @author: 朱江
 * @description:
 * @date: 2023/6/6
 **/
@Data
@ToString
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    List<TeachplanDto> teachPlanTreeNodes;

}
