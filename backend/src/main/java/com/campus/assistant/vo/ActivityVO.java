package com.campus.assistant.vo;

import com.campus.assistant.entity.Activity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityVO {

    private Long id;
    private String title;
    private Long venueId;
    private String location;
    private String coverUrl;
    private String content;
    private Integer capacity;
    private Integer enrolledCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkinStartTime;
    private LocalDateTime checkinEndTime;
    private Integer status;
    private Boolean enrolled;
    private Boolean checkedIn;

    public static ActivityVO from(Activity activity, boolean enrolled, boolean checkedIn) {
        return ActivityVO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .venueId(activity.getVenueId())
                .location(activity.getLocation())
                .coverUrl(activity.getCoverUrl())
                .content(activity.getContent())
                .capacity(activity.getCapacity())
                .enrolledCount(activity.getEnrolledCount())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .checkinStartTime(activity.getCheckinStartTime())
                .checkinEndTime(activity.getCheckinEndTime())
                .status(activity.getStatus())
                .enrolled(enrolled)
                .checkedIn(checkedIn)
                .build();
    }
}
