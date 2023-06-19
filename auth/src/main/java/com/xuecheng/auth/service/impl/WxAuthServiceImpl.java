package com.xuecheng.auth.service.impl;

import com.xuecheng.auth.dto.AuthParamsDto;
import com.xuecheng.auth.dto.XcUserExt;
import com.xuecheng.auth.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * @className: WxAuthServiceImpl
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
@Service("wx_service")
public class WxAuthServiceImpl implements AuthService {
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
