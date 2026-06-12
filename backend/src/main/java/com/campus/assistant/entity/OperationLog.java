package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体，对应 ca_operation_log 表
 */
@Data
@TableName("ca_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String operation;
    private String bizType;
    private Long bizId;
    private String detail;
    private LocalDateTime createTime;
}
