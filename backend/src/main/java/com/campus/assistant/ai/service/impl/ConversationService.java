package com.campus.assistant.ai.service.impl;

import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 多轮对话会话服务，负责管理用户的 AI 对话历史和上下文。
 */
@Service
@RequiredArgsConstructor
public class ConversationService {

    private static final String CONVERSATION_PREFIX = "ca:conversation:";
    private static final String CONVERSATION_LIST_PREFIX = "ca:conversation:list:";
    private static final int MAX_HISTORY = 20;
    private static final long TTL_HOURS = 24;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 对话消息记录
     */
    public record ChatMessage(String role, String content, String time) {}

    /**
     * 获取或创建会话ID
     */
    public String getOrCreateSession(Long userId, String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        String newId = UUID.randomUUID().toString().substring(0, 8);
        // 记录到用户会话列表
        String listKey = CONVERSATION_LIST_PREFIX + userId;
        Map<String, String> sessionMeta = new HashMap<>();
        sessionMeta.put("title", "新对话");
        sessionMeta.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        stringRedisTemplate.opsForHash().putAll(listKey + ":" + newId, sessionMeta);
        stringRedisTemplate.expire(listKey + ":" + newId, TTL_HOURS, TimeUnit.HOURS);
        return newId;
    }

    /**
     * 获取用户的所有会话列表
     */
    public List<Map<String, String>> getSessionList(Long userId) {
        String listKey = CONVERSATION_LIST_PREFIX + userId;
        List<Map<String, String>> sessions = new ArrayList<>();
        var keys = stringRedisTemplate.keys(listKey + ":*");
        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                Map<String, String> session = new HashMap<>();
                session.put("id", key.substring(key.lastIndexOf(":") + 1));
                session.put("title", String.valueOf(entries.getOrDefault("title", "未命名")));
                session.put("createdAt", String.valueOf(entries.getOrDefault("createdAt", "")));
                sessions.add(session);
            }
        }
        sessions.sort(Comparator.comparing(s -> s.get("createdAt"), Comparator.reverseOrder()));
        return sessions.isEmpty() ? null : sessions;
    }

    /**
     * 保存一条消息到会话历史
     */
    public void saveMessage(Long userId, String sessionId, String role, String content) {
        String key = CONVERSATION_PREFIX + userId + ":" + sessionId;
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        msg.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        try {
            String msgJson = objectMapper.writeValueAsString(msg);
            stringRedisTemplate.opsForList().rightPush(key, msgJson);
            stringRedisTemplate.opsForList().trim(key, -MAX_HISTORY, -1);
            stringRedisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception ignored) {}
    }

    /**
     * 获取会话历史消息
     */
    public List<ChatMessage> getHistory(Long userId, String sessionId) {
        String key = CONVERSATION_PREFIX + userId + ":" + sessionId;
        List<String> rawList = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<ChatMessage> history = new ArrayList<>();
        if (rawList != null) {
            for (String raw : rawList) {
                try {
                    ChatMessage msg = objectMapper.readValue(raw, ChatMessage.class);
                    history.add(msg);
                } catch (Exception ignored) {}
            }
        }
        return history;
    }

    /**
     * 删除一个会话
     */
    public void deleteSession(Long userId, String sessionId) {
        String key = CONVERSATION_PREFIX + userId + ":" + sessionId;
        String listKey = CONVERSATION_LIST_PREFIX + userId + ":" + sessionId;
        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(listKey);
    }

    /**
     * 更新会话标题
     */
    public void updateTitle(Long userId, String sessionId, String title) {
        String listKey = CONVERSATION_LIST_PREFIX + userId + ":" + sessionId;
        stringRedisTemplate.opsForHash().put(listKey, "title", title);
        stringRedisTemplate.expire(listKey, TTL_HOURS, TimeUnit.HOURS);
    }
}
