package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @className: MediaServiceFallbackFactory
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/

@Slf4j
@Component
public class MediaServiceFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return (upload, objectName) -> {
            log.debug("远程调用时，上传文件接口发生熔断:{}", throwable.toString(), throwable);
            return null;
        };
    }
}
