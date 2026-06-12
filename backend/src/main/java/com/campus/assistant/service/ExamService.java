package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.ExamSaveDTO;
import com.campus.assistant.entity.Exam;

/**
 * 考试服务接口，定义考试查询和后台管理相关业务能力。
 */
public interface ExamService {

    /**
     * 考试分页查询，支持按院系、日期和类型筛选
     */
    Page<Exam> page(Long current, Long size, String college, String examType, String keyword);

    /**
     * 考试详情
     */
    Exam detail(Long id);

    /**
     * 管理端考试分页
     */
    Page<Exam> managePage(Long current, Long size);

    /**
     * 新增考试
     */
    Long save(ExamSaveDTO dto);

    /**
     * 编辑考试
     */
    void update(ExamSaveDTO dto);

    /**
     * 删除考试
     */
    void delete(Long id);
}
