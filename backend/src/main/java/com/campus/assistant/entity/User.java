package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 ca_user 表
 */
@Data
@TableName("ca_user")
public class User {

    /**
     * 用户ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号（学号/工号），用于系统登录
     */
    private String username;

    /**
     * 登录密码，BCrypt加密存储
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 学号，仅学生角色有值
     */
    private String studentNo;

    /**
     * 所属学院
     */
    private String college;

    /**
     * 身份证后6位，用于身份验证
     */
    private String idCardLast6;

    /**
     * 角色编码：STUDENT(学生)、TEACHER(教师)、ADMIN(管理员)
     */
    private String roleCode;

    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 初始密码标记：0-已修改密码，1-使用初始密码
     */
    private Integer initialPassword;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
