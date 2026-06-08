package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ?? ???????????????????
 */
@Data
@TableName("ca_venue")
public class Venue {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String location;
    private String imageUrl;
    private Integer capacity;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
