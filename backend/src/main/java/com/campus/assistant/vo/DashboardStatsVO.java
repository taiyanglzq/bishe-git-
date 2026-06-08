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
    private List<NameValueVO> bookingStatus;
    private List<NameValueVO> activityEnrollRank;
    private List<NameValueVO> venueBookingRank;
    private Long enrollCount;
    private Long checkinCount;
    private Double checkinRate;

    @Data
    @Builder
    public static class NameValueVO {
        private String name;
        private Long value;
    }
}
