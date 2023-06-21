package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.dto.CourseBaseInfoDto;
import com.xuecheng.content.dto.CoursePreviewDto;
import com.xuecheng.content.dto.TeachplanDto;
import com.xuecheng.content.entity.CourseBase;
import com.xuecheng.content.entity.CourseMarket;
import com.xuecheng.content.entity.CoursePublish;
import com.xuecheng.content.entity.CoursePublishPre;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.entity.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    CourseBaseService courseBaseService;

    @Autowired
    CourseTeacherService courseTeacherService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    MqMessageService messageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseService.getCourseBaseInfoDto(courseId));
        coursePreviewDto.setTeachPlans(teachplanService.getTreeNodes(courseId));
        coursePreviewDto.setTeacher(courseTeacherService.getTeacherInfo(courseId));
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //约束校验
        CourseBase courseBase = courseBaseService.getById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            XuechengException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XuechengException.cast("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XuechengException.cast("提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.getCourseBaseInfoDto(courseId);
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.getTreeNodes(courseId);
        if (teachplanTree.size() == 0) {
            XuechengException.cast("提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseService.getBaseMapper().updateById(courseBase);
    }

    /**
     * @Author: 朱江
     * @Description: 课程发布服务，需要从预发布表读取数据到发布表，并写入消息处理表
     * @Date: 15:33 2023/6/14
     **/
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null)
            XuechengException.cast("请先提交课程审核，审核通过才可以发布");
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            XuechengException.cast("不允许提交其它机构的课程。");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if (!"202004".equals(auditStatus)) {
            XuechengException.cast("操作失败，课程审核通过方可发布。");
        }

        // 保存课程发布信息
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        boolean isExist = getBaseMapper().selectById(courseId) != null;
        if (isExist) baseMapper.updateById(coursePublish);
        else baseMapper.insert(coursePublish);

        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseService.getBaseMapper().selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseService.getBaseMapper().updateById(courseBase);

        //todo 保存消息表
        MqMessage message = messageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (message == null) XuechengException.cast(CommonError.UNKNOWN_ERROR);
        // 删除预发布表中的对应课程信息
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
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
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content, "UTF-8");
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}", e.toString());
            XuechengException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
        if (course == null) {
            XuechengException.cast("上传静态文件异常");
        }

    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return this.getBaseMapper().selectById(courseId);
    }
}
