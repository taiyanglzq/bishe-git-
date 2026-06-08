package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ????? ??????????????????????
 */
@Data
@TableName("ca_venue_slot")
public class VenueSlot {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long venueId;
    private LocalDate slotDate;
    private String timeRange;
    private Integer totalQuota;
    private Integer remainingQuota;
    private Integer status;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
