package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体，对应 ca_course 表
 */
@Data
@TableName("ca_course")
public class Course {

    /**
     * 课程ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 授课教师
     */
    private String teacherName;

    /**
     * 开课院系
     */
    private String college;

    /**
     * 学期，如 2025-2026-2
     */
    private String semester;

    /**
     * 上课教室
     */
    private String classroom;

    /**
     * 上课时间，如 周一 8:00-9:40，周三 10:00-11:40
     */
    private String scheduleInfo;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 课程容量
     */
    private Integer capacity;

    /**
     * 课程简介
     */
    private String description;

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
