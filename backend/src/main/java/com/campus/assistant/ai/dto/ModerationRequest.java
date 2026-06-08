package com.campus.assistant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * ???? DTO???????????????????
 */
@Data
@Builder
public class ModerationRequest {

    @NotBlank(message = "审核内容不能为空")
    private String content;

    private String scene;
    private Long userId;
}
