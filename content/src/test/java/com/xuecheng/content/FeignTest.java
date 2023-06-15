package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @className: FeignTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/
@SpringBootTest
public class FeignTest {
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Test
    public void test() {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\test.html"));
        mediaServiceClient.upload(multipartFile,"course/test.html");
    }
}
