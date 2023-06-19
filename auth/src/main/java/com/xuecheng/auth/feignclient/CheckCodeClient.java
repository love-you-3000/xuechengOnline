package com.xuecheng.auth.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @className: CheckCodeClient
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
@FeignClient(value = "checkcode",fallbackFactory = CheckCodeClientFactory.class)
public interface CheckCodeClient {
    @PostMapping(value = "/checkcode/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);

}
