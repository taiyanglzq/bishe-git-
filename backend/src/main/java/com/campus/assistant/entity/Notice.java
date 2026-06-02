package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ca_notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;
    private String content;
    private Integer status;
    private Long viewCount;
    private Long publisherId;
    private String scopeType;
    private String scopeCollege;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
