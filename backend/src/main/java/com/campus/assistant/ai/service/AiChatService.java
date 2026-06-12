package com.campus.assistant.ai.service;

import com.campus.assistant.ai.dto.AiChatRequest;
import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.entity.User;

import java.util.List;
import java.util.Map;

/**
 * AI 对话服务接口，支持多轮会话管理。
 */
public interface AiChatService {

    /**
     * 发送消息并获取回复，支持多轮对话上下文
     */
    AiChatResponse chat(AiChatRequest request, User currentUser);

    /**
     * 获取用户的会话列表
     */
    List<Map<String, String>> getSessions(User currentUser);

    /**
     * 获取指定会话的历史消息
     */
    List<?> getHistory(User currentUser, String sessionId);

    /**
     * 删除指定会话
     */
    void deleteSession(User currentUser, String sessionId);
}
