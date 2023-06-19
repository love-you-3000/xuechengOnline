package com.xuecheng.auth.entity;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @className: LoginUser
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
public class LoginUser implements UserDetails {
    private final XcUser user;
    private final String password;

    public LoginUser(XcUser user) {
        this.user = user;
        this.password = user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        user.setPassword(null);
        return JSON.toJSONString(user);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
