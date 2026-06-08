package com.campus.assistant.ai.dto;

import com.campus.assistant.ai.enums.ModerationResult;
import lombok.Builder;
import lombok.Data;

/**
 * ???? DTO???????????????????
 */
@Data
@Builder
public class ModerationResponse {

    private ModerationResult result;
    private String reason;
    private String suggestion;
}
