package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * ???? DTO???????????????????
 */
@Data
public class BookingCreateDTO {

    @NotNull(message = "场地不能为空")
    private Long venueId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;

    @NotBlank(message = "预约时间段不能为空")
    private String timeRange;

    @NotBlank(message = "预约原因不能为空")
    private String reason;
}
