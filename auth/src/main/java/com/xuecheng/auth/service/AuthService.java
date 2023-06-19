package com.xuecheng.auth.service;

import com.xuecheng.auth.dto.AuthParamsDto;
import com.xuecheng.auth.dto.XcUserExt;

/**
 * @className: AuthService
 * @author: 朱江
 * @description:
 * @date: 2023/6/19
 **/
public interface AuthService {
    /**
     * @Author: 朱江
     * @Description:  认证方法接口
     * @Date: 16:58 2023/6/19
    **/
    XcUserExt execute(AuthParamsDto authParamsDto);
}
