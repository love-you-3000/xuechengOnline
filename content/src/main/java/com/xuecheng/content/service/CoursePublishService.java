package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.dto.CoursePreviewDto;
import com.xuecheng.content.entity.CoursePublish;

import java.io.File;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-06-03
 */
public interface CoursePublishService extends IService<CoursePublish> {
    /**
     * @Author: 朱江
     * @Description: 获取课程发布的所有相关信息
     * @Date: 9:41 2023/6/13
    **/
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);

    /**
     * @Author: 朱江
     * @Description: 课程静态化
     * @Date: 16:15 2023/6/15
    **/
    File generateCourseHtml(Long courseId);

    void  uploadCourseHtml(Long courseId,File file);

    CoursePublish getCoursePublish(Long courseId);
}
