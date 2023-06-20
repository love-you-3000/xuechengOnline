package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XuechengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.dto.AddCourseDto;
import com.xuecheng.content.dto.CourseBaseInfoDto;
import com.xuecheng.content.dto.EditCourseDto;
import com.xuecheng.content.dto.QueryCourseParamsDto;
import com.xuecheng.content.entity.*;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseTeacherMapper teacherMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper mediaMapper;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //测试查询接口
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 课程名称，模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        // 根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        queryWrapper.eq(CourseBase::getCompanyId, companyId);
        //分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //分页查询E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //数据
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();
        //准备返回数据 List<T> items, long counts, long page, long pageSize
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XuechengException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XuechengException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XuechengException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XuechengException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XuechengException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XuechengException("适应人群");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XuechengException("收费规则为空");
        }
        CourseBase courseBase = new CourseBase();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        // todo 创建人和更新人加了认证后加入
        // 审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0) {
            throw new RuntimeException("更新课程失败！");
        }
        BeanUtils.copyProperties(dto, courseMarket);
        Long CourseId = courseBase.getId();
        courseMarket.setId(CourseId);
        if (saveCourseMarket(courseMarket) <= 0) {
            throw new RuntimeException("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(CourseId);
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfoDto(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto dto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, dto);
        if (courseMarket != null) BeanUtils.copyProperties(courseMarket, dto);
        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        dto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        dto.setMtName(courseCategoryByMt.getName());
        return dto;
    }

    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        //收费规则
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new XuechengException("收费规则没有选择");
        }
        //收费规则为收费
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
                throw new XuechengException("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarketObj == null) {
            return courseMarketMapper.insert(courseMarketNew);
        } else {
            BeanUtils.copyProperties(courseMarketNew, courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }


    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) throw new XuechengException("课程不存在！");
        //校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            throw new XuechengException("不能修改其他机构的课程！");
        }
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        if (courseBaseMapper.updateById(courseBase) <= 0) {
            throw new RuntimeException("更新课程失败！");
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null) {
            BeanUtils.copyProperties(dto, courseMarket);
            saveCourseMarket(courseMarket);
        }
        return getCourseBaseInfoDto(courseId);


    }

    @Transactional
    @Override
    public void deleteCourse(Long courseId) {
        // 删除课程需要删除课程相关的基本信息、营销信息、课程计划、课程教师信息。
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程的审核状态为未提交时方可删除
        if (!courseBase.getAuditStatus().equals("202002"))
            throw new XuechengException("课程已提交不能删除！");
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanWrapper = new LambdaQueryWrapper<>();
        teachplanWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanWrapper);
        // 删除媒资信息
        LambdaQueryWrapper<TeachplanMedia> mediaWrapper = new LambdaQueryWrapper<>();
        mediaWrapper.eq(TeachplanMedia::getCourseId, courseId);
        mediaMapper.delete(mediaWrapper);
        // 删除教师信息
        LambdaQueryWrapper<CourseTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(CourseTeacher::getCourseId, courseId);
        teacherMapper.delete(teacherWrapper);
        // 最后删除课程信息
        courseBaseMapper.deleteById(courseId);
    }

}
