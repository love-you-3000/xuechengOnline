package com.xuecheng.content.feignclient;

import com.xuecheng.content.entity.CourseIndex;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @className: SearchClient
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/
@FeignClient(value = "search")
public interface SearchClient {
    @ApiOperation("添加课程索引")
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
