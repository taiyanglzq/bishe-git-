package com.campus.assistant.ai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * AI ?? DTO????????AI ?????????
 */
@Data
@Builder
public class AiChatResponse {

    private String answer;
    private String intent;
    private String suggestedAction;
}
