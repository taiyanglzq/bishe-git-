package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ???? VO???????????????????
 */
@Data
@Builder
public class ActivityRecordVO {

    private Long id;
    private Long activityId;
    private String activityTitle;
    private String status;
    private LocalDateTime enrollTime;
    private LocalDateTime checkinTime;
}
