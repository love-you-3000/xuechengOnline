package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @className: MediaServiceClientFallBack
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/


public class MediaServiceClientFallBack implements MediaServiceClient{
    @Override
    public String upload(MultipartFile upload, String objectName) {
        return null;
    }
}
