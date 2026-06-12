package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名实体，对应 ca_activity_enroll 表
 */
@Data
@TableName("ca_activity_enroll")
public class ActivityEnroll {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long studentId;
    private String status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
