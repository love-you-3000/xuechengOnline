package com.xuecheng.learning;

import com.xuecheng.content.entity.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @className: FeignClientTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/21
 **/
@SpringBootTest
public class FeignClientTest {
    @Autowired
    ContentServiceClient contentServiceClient;

    @Test
    public void TestCoursePublish()
    {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(1L);
        System.out.println(coursepublish);
    }
}
