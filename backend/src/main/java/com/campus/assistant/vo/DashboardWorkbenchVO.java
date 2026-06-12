package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ??? VO??????????????????
 */
@Data
@Builder
public class DashboardWorkbenchVO {

    private String roleName;
    private String welcomeText;
    private Long unreadNotificationCount;
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
