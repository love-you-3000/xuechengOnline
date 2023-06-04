package com.xuecheng.content.dto;

import com.xuecheng.content.entity.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @className: CourseCategoryTreeDto
 * @author: 朱江
 * @description:
 * @date: 2023/6/4
 **/
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategory> childrenTreeNodes;

    public List<CourseCategory> getChildrenTreeNodes() {
        return childrenTreeNodes;
    }

    public void setChildrenTreeNodes(List<CourseCategory> childrenTreeNodes) {
        this.childrenTreeNodes = childrenTreeNodes;
    }
}
