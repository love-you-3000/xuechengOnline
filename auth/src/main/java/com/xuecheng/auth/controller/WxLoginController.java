package com.xuecheng.auth.controller;

import com.xuecheng.auth.entity.XcUser;
import com.xuecheng.auth.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @className: WxLoginController
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
@Slf4j
@Controller
public class WxLoginController {
    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据|库

        XcUser xcUser = wxAuthService.wxAuth(code);
        //暂时硬编写，目的是调试环境
        if (xcUser == null) {
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + xcUser.getUsername() + "&authType=wx";
    }
}
