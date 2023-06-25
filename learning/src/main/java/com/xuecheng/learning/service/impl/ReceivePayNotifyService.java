package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.entity.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.xuecheng.learning.config.PayNotifyConfig.PAY_NOTIFY_QUEUE;

/**
 * @className: ReceivePayNotifyService
 * @author: 朱江
 * @description:
 * @date: 2023/6/25
 **/
@Service
@Slf4j
public class ReceivePayNotifyService {

    @Autowired
    MyCourseTablesService myCourseTablesService;
    @RabbitListener(queues = PAY_NOTIFY_QUEUE)
    public void receive(Message message) {
        MqMessage mqMessage = JSON.parseObject(new String(message.getBody()), MqMessage.class);
        // 选课ID
        String businessKey1 = mqMessage.getBusinessKey1();
        // 订单类型
        String businessKey2 = mqMessage.getBusinessKey2();
        if (businessKey2.equals("60201"))
        {
            // 根据消息内容更新对应的表
            boolean b = myCourseTablesService.saveChooseCourseSuccess(businessKey1);
            if(!b) XuechengException.cast("保存选课记录失败！");
        }

    }
}
