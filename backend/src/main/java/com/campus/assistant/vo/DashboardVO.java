package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardVO {

    private Long userCount;
    private Long noticeCount;
    private Long venueCount;
    private Long activityCount;
    private Long bookingCount;
    private Long checkinCount;
}
