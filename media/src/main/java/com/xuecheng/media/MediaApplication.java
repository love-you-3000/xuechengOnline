package com.xuecheng.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @className: MediaApplication
 * @author: 朱江
 * @description:
 * @date: 2023/6/7
 **/

@SpringBootApplication
public class MediaApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MediaApplication.class);
    public static void main(String[] args) {
        SpringApplication app =new  SpringApplication(MediaApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        LOG.info("启动成功！");
        LOG.info("地址:\thttp://localhost:{}",env.getProperty("server.port"));
    }
}
