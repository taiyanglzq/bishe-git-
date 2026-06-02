package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ca_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String studentNo;
    private String college;
    private String idCardLast6;
    private String roleCode;
    private Integer status;
    private Integer initialPassword;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
