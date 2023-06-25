package com.xuecheng;

import com.xuecheng.messagesdk.service.MqMessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class OrdersApiApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OrdersApiApplication.class, args);
        MqMessageService bean = context.getBean(MqMessageService.class);
        System.out.println(bean);
    }
}
