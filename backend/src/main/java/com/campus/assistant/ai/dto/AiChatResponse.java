package com.campus.assistant.ai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI 对话响应 DTO，包含多轮会话标识和 RAG 标记。
 */
@Data
@Builder
public class AiChatResponse {

    private String answer;
    private String intent;
    private String suggestedAction;
    private boolean ragUsed;
    private String sessionId;
}
