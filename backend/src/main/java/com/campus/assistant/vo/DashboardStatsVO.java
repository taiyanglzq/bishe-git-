package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ???? VO???????????????????
 */
@Data
@Builder
public class DashboardStatsVO {

    private DashboardVO summary;

    @Data
    @Builder
    public static class NameValueVO {
        private String name;
        private Long value;
    }
}
