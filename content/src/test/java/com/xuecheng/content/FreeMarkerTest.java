package com.xuecheng.content;

import com.xuecheng.content.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @className: FreeMarkerTest
 * @author: 朱江
 * @description:
 * @date: 2023/6/15
 **/
@SpringBootTest
public class FreeMarkerTest {
    @Autowired
    CoursePublishService coursePublishService;

    @Test
    public void testGeneratorHtml() throws IOException, TemplateException {
        // 页面静态化测试
        //配置freemarker
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding("utf-8");
        URL path = this.getClass().getResource("/templates");
        //加载模板
        //选指定模板路径,classpath下templates下
        //得到classpath路径
        String decodedPath = URLDecoder.decode(path.getFile(), "UTF-8");
        configuration.setDirectoryForTemplateLoading(new File(decodedPath));
        //设置字符编码
        //指定模板文件名称
        Template template = configuration.getTemplate("course_template.ftl");
        //准备数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(203L);

        Map<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);
        //静态化
        //参数1：模板，参数2：数据模型
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(content);
        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(content, "UTF-8");
        //输出流
        FileOutputStream outputStream = new FileOutputStream("D:\\test.html");
        IOUtils.copy(inputStream, outputStream);
    }
}
