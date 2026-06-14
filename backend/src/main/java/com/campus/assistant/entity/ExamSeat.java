package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试座位安排实体，对应 ca_exam_seat 表
 */
@Data
@TableName("ca_exam_seat")
public class ExamSeat {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 座位号，如 A12、B05 */
    private String seatNo;

    /** 所属院系 */
    private String college;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
