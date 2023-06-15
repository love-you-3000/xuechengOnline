package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @className: MediaServiceClient
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/
@FeignClient(value = "media", configuration = {MultipartSupportConfig.class}, fallbackFactory = MediaServiceFallbackFactory.class)
public interface MediaServiceClient {
    @ApiOperation("上传文件")
    @PostMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "objectName", required = false) String objectName);
}
