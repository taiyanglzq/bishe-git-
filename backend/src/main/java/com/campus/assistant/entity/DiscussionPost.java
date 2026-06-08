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
@TableName("ca_discussion_post")
public class DiscussionPost {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long authorId;
    private String college;
    private String title;
    private String content;
    private String imageUrl;
    private Integer pinned;
    private Integer featured;
    private Long likeCount;
    private Long commentCount;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
