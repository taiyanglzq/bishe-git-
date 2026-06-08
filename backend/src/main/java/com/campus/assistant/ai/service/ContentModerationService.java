package com.campus.assistant.ai.service;

import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;/**
 * ???? ???????????????????????
 */
public interface ContentModerationService {

    ModerationResponse moderate(ModerationRequest request);
}
