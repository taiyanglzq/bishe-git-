package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场地预约实体，对应 ca_booking 表
 */
@Data
@TableName("ca_booking")
public class Booking {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long venueId;
    private LocalDate bookingDate;
    private String timeRange;
    private String reason;
    private String status;
    private Long auditUserId;
    private String auditRemark;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
