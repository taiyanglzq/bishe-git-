package com.campus.assistant.ai.service;

import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.entity.User;/**
 * AI ?? ???????AI ??????????????
 */
public interface AiChatService {

    AiChatResponse chat(String question, User currentUser);
}
