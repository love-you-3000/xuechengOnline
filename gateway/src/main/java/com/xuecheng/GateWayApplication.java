package com.xuecheng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @className: GateWayApplication
 * @author: 朱江
 * @description:
 * @date: 2023/6/7
 **/
@SpringBootApplication
public class GateWayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(GateWayApplication.class);
    public static void main(String[] args) {
        SpringApplication app =new  SpringApplication(GateWayApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();
        LOG.info("网关启动成功！");
        LOG.info("地址:\thttp://localhost:{}",env.getProperty("server.port"));
    }
}
