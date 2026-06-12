package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签到实体，对应 ca_checkin 表
 */
@Data
@TableName("ca_checkin")
public class Checkin {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long studentId;
    private LocalDateTime checkinTime;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
