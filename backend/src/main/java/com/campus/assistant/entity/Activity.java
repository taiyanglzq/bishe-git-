package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体，对应 ca_activity 表
 */
@Data
@TableName("ca_activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long venueId;
    private String location;
    private String coverUrl;
    private String content;
    private Integer capacity;
    private Integer enrolledCount;
    private Long publisherId;
    private String scopeType;
    private String scopeCollege;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkinStartTime;
    private LocalDateTime checkinEndTime;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
