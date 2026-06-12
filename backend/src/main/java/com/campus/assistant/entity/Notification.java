package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体，对应 ca_notification 表
 */
@Data
@TableName("ca_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private Integer readStatus;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
