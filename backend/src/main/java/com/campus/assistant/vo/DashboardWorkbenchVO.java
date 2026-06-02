package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardWorkbenchVO {

    private String roleName;
    private String welcomeText;
    private Long pendingBookingCount;
    private Long unreadNotificationCount;
    private Long todayBookingCount;
    private Long upcomingActivityCount;
    private List<WorkbenchItemVO> todos;
    private List<WorkbenchItemVO> schedules;

    @Data
    @Builder
    public static class WorkbenchItemVO {
        private String title;
        private String description;
        private String type;
        private String timeText;
        private Long bizId;
    }
}
