package com.campus.assistant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 对话请求 DTO，支持多轮对话会话标识。
 */
@Data
public class AiChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 可选：会话ID，用于多轮对话上下文。不传则自动创建新会话。
     */
    private String sessionId;
}
