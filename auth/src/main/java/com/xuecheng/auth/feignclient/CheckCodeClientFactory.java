package com.xuecheng.auth.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @className: CheckCodeClientFactory
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return (key, code) -> {
            log.debug("调用验证码服务熔断异常:{}", throwable.getMessage());
            return null;
        };
    }
}
