package com.xuecheng.content;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @className: ContentApplication
 * @author: 朱江
 * @description:
 * @date: 2023/6/3
 **/

@SpringBootApplication
@ComponentScan("com.xuecheng")
@MapperScan("com.xuecheng.content.mapper")
public class ContentApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ContentApplication.class);
    public static void main(String[] args) {
        SpringApplication app =new  SpringApplication(ContentApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        LOG.info("启动成功！");
        LOG.info("地址:\thttp://localhost:{}",env.getProperty("server.port"));
    }
}
