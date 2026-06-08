package com.campus.assistant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ???? DTO???????????????????
 */
@Data
public class DiscussionBanDTO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    private String reason;
}
