package com.xuecheng.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.auth.dto.AuthParamsDto;
import com.xuecheng.auth.dto.XcUserExt;
import com.xuecheng.auth.mapper.XcUserMapper;
import com.xuecheng.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @className: UserServiceImpl
 * @author: 朱江
 * @description: 实现UserDetailsService实现从数据库读取用户信息
 * @date: 2023/6/19
 **/

@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper userMapper;

    @Autowired
    ApplicationContext applicationContext;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_service", AuthService.class);
        XcUserExt userExt = authService.execute(authParamsDto);
        return getUserDetail(userExt);
    }

    private UserDetails getUserDetail(XcUserExt userExt) {
        userExt.getPermissions().add("p1");
        String[] permission = new ArrayList<>(userExt.getPermissions()).toArray(new String[0]);
        String userJson = JSON.toJSONString(userExt);
        return User.withUsername(userJson).password(userExt.getPassword()).authorities(permission).build();
    }
}
