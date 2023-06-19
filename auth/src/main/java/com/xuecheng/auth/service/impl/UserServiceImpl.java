package com.xuecheng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.entity.LoginUser;
import com.xuecheng.auth.entity.XcUser;
import com.xuecheng.auth.mapper.XcUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @className: UserServiceImpl
 * @author: 朱江
 * @description: 实现UserDetailsService实现从数据库读取用户信息
 * @date: 2023/6/19
 **/

@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名在数据库查找用户
        LambdaQueryWrapper<XcUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcUser::getUsername, username);
        XcUser xcUser = userMapper.selectOne(wrapper);
        // 如果没有查询到，返回null
        if (xcUser == null) return null;
        // 将密码和用户名封装成UserDetail，由Security框架进行密码比对
        return new LoginUser(xcUser);
    }
}
