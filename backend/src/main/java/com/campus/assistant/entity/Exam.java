package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考试安排实体，对应 ca_exam 表
 */
@Data
@TableName("ca_exam")
public class Exam {

    /**
     * 考试ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联课程ID
     */
    private Long courseId;

    /**
     * 考试科目名称
     */
    private String courseName;

    /**
     * 考试日期
     */
    private LocalDate examDate;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 考试地点
     */
    private String location;

    /**
     * 座位号
     */
    private String seatNo;

    /**
     * 考试类型：期末考试、期中考试、补考
     */
    private String examType;

    /**
     * 所属院系
     */
    private String college;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
