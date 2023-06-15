package com.xuecheng.content.service.jobhandler;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.entity.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @className: CoursePublishTask
 * @author: 朱江
 * @description:
 * @date: 2023/6/14
 **/

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() {
        // 分片参数
        int shardTotal = XxlJobHelper.getShardTotal();
        int shardIndex = XxlJobHelper.getShardIndex();
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {

        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());
        saveCourseEs(mqMessage, courseId);
        saveCourseRedis(mqMessage, courseId);
        generateCourseHtml(mqMessage, courseId);
        return true;

    }

    private void saveCourseEs(MqMessage mqMessage, Long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("索引写入ES已执行, 无需重复处理...");
            return;
        }
        // todo 存入es

        mqMessageService.completedStageOne(taskId);
    }

    private void saveCourseRedis(MqMessage mqMessage, Long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("数据写入缓存已执行, 无需重复处理...");
            return;
        }
        // 写入redis
        mqMessageService.completedStageTwo(taskId);
    }

    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始进行课程静态化,课程id:{}", courseId);
        // 保证幂等性
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            log.debug("页面静态化存储已执行, 无需重复处理...");
            return;
        }
        // 保存静态页面到minio
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file != null) {
            coursePublishService.uploadCourseHtml(courseId, file);
            mqMessageService.completedStageThree(taskId);
        }
    }
}
