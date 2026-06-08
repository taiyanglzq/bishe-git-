package com.campus.assistant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ?? DTO?????????????????
 */
@Data
public class AuditDTO {

    @NotNull(message = "预约单不能为空")
    private Long bookingId;

    private String remark;
}
