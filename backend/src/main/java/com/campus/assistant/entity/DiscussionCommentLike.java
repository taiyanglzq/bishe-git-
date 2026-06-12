package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讨论区评论点赞实体，对应 ca_discussion_comment_like 表
 */
@Data
@TableName("ca_discussion_comment_like")
public class DiscussionCommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
