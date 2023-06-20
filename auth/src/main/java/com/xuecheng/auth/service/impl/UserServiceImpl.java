package com.xuecheng.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.auth.dto.AuthParamsDto;
import com.xuecheng.auth.dto.XcUserExt;
import com.xuecheng.auth.entity.XcMenu;
import com.xuecheng.auth.mapper.XcMenuMapper;
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
import java.util.List;

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
    XcMenuMapper xcMenuMapper;

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
        String password = userExt.getPassword();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userExt.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size() == 0) {
            //用户权限,如果不加则报Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        } else {
            xcMenus.forEach(menu -> {
                permissions.add(menu.getCode());
            });
        }
        //为了安全在令牌中不放密码
        userExt.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(userExt);
        //将用户权限放在XcUserExt中
        userExt.setPermissions(permissions);
        String[] permission = new ArrayList<>(permissions).toArray(new String[0]);
        return User.withUsername(userString).password(password).authorities(permission).build();
    }
}
