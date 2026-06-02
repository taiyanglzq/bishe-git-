package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveDTO {

    private Long id;

    @NotBlank(message = "活动名称不能为空")
    private String title;

    @NotNull(message = "活动场地不能为空")
    private Long venueId;

    private String location;
    private String content;

    @NotNull(message = "活动容量不能为空")
    private Integer capacity;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime checkinStartTime;
    private LocalDateTime checkinEndTime;
    private String scopeType;
    private String scopeCollege;
    private Integer status;
}
