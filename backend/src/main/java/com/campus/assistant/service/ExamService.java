package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.ExamSaveDTO;
import com.campus.assistant.dto.ExamSeatGenerateDTO;
import com.campus.assistant.entity.Exam;
import com.campus.assistant.entity.ExamSeat;

import java.util.List;

/**
 * 考试服务接口，定义考试查询、后台管理和座位安排相关业务能力。
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

    // ========== 座位管理 ==========

    /**
     * 查询考试座位列表
     */
    List<ExamSeat> getSeats(Long examId);

    /**
     * 生成座位
     */
    void generateSeats(ExamSeatGenerateDTO dto);

    /**
     * 更新单个座位号
     */
    void updateSeat(Long seatId, String seatNo);

    /**
     * 导出座位表 Excel
     */
    byte[] exportSeats(Long examId);
}
