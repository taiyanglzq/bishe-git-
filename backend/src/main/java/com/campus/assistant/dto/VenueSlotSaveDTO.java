package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VenueSlotSaveDTO {

    private Long id;

    @NotNull(message = "场地不能为空")
    private Long venueId;

    @NotNull(message = "日期不能为空")
    private LocalDate slotDate;

    @NotBlank(message = "时间段不能为空")
    private String timeRange;

    @NotNull(message = "总名额不能为空")
    private Integer totalQuota;

    private Integer remainingQuota;
    private Integer status;
}
