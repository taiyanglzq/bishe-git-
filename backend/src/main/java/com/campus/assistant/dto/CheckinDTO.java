package com.campus.assistant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckinDTO {

    @NotNull(message = "活动不能为空")
    private Long activityId;
}
