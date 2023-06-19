package com.xuecheng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.dto.AuthParamsDto;
import com.xuecheng.auth.dto.XcUserExt;
import com.xuecheng.auth.entity.XcUser;
import com.xuecheng.auth.feignclient.CheckCodeClient;
import com.xuecheng.auth.mapper.XcUserMapper;
import com.xuecheng.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @className: PasswordAuthServiceImpl
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
@Service("password_service")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    XcUserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String checkcodekey = authParamsDto.getCheckcodekey();
        String checkcode = authParamsDto.getCheckcode();
        if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey))
            throw new RuntimeException("请输入验证码！");
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) throw new RuntimeException("验证码错误！");
        String username = authParamsDto.getUsername();
        XcUser user = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) throw new RuntimeException("账号不存在！");
        boolean isMatch = passwordEncoder.matches(authParamsDto.getPassword(), user.getPassword());
        if (!isMatch) throw new RuntimeException("账号或密码错误！");
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
