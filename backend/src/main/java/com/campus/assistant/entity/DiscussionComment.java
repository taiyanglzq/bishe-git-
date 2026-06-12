package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讨论区评论实体，对应 ca_discussion_comment 表
 */
@Data
@TableName("ca_discussion_comment")
public class DiscussionComment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Long likeCount;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
