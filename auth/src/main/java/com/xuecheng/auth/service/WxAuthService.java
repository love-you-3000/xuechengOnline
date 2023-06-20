package com.xuecheng.auth.service;

import com.xuecheng.auth.entity.XcUser;

/**
 * @className: WxAuthService
 * @author: 朱江
 * @description: 微信认证接口
 * @date: 2023/6/20
 **/
public interface WxAuthService {
    XcUser wxAuth(String code);
}
