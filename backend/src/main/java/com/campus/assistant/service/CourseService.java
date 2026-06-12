package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.CourseSaveDTO;
import com.campus.assistant.entity.Course;

/**
 * 课程服务接口，定义课程查询和后台管理相关业务能力。
 */
public interface CourseService {

    /**
     * 课程分页查询，支持按院系和学期筛选
     */
    Page<Course> page(Long current, Long size, String college, String semester, String keyword);

    /**
     * 课程详情
     */
    Course detail(Long id);

    /**
     * 管理端课程分页
     */
    Page<Course> managePage(Long current, Long size);

    /**
     * 新增课程
     */
    Long save(CourseSaveDTO dto);

    /**
     * 编辑课程
     */
    void update(CourseSaveDTO dto);

    /**
     * 删除课程
     */
    void delete(Long id);
}
