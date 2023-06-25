package com.xuecheng.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.xuecheng.messagesdk")
public class OrdersApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersApiApplication.class, args);
    }

}
