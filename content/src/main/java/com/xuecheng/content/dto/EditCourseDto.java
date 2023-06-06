package com.xuecheng.content.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @className: EditCourseDto
 * @author: 朱江
 * @description:
 * @date: 2023/6/6
 **/

@Data
@ApiModel(value = "EditCourseDto", description = "修改课程基本信息")

public class EditCourseDto extends AddCourseDto {
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
