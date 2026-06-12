package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体，对应 ca_notice 表
 */
@Data
@TableName("ca_notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String category;  //公告
    private String content;
    private Integer status;
    private Long viewCount;//浏览次数/阅读量
    private Long publisherId;//发布者ID
    private String scopeType;//可见范围类型：SCHOOL(全校可见)、COLLEGE(仅本院可见)
    private String scopeCollege;//可见范围学院，当 scopeType=COLLEGE 时指定具体学院名称
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
