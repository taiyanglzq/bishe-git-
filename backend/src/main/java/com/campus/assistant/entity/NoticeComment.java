package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ???? ?????????????????????
 */
@Data
@TableName("ca_notice_comment")
public class NoticeComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noticeId;
    private Long userId;
    private String content;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
