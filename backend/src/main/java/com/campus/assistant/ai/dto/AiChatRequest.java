package com.campus.assistant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI ?? DTO????????AI ?????????
 */
@Data
public class AiChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}
