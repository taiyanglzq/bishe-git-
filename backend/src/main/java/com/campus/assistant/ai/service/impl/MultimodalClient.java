package com.campus.assistant.ai.service.impl;

import com.campus.assistant.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模态 AI 客户端，使用通义千问 Vision 模型处理文本+图片输入。
 */
@Component
@RequiredArgsConstructor
public class MultimodalClient {

    private final RestClient qwenRestClient;
    private final AiProperties aiProperties;

    /**
     * 发送多模态请求到通义千问（文本+图片）
     */
    public String chat(String systemPrompt, String userText, List<String> base64Images) {
        if (!aiProperties.isEnabled()
                || aiProperties.getQwenApiKey() == null
                || aiProperties.getQwenApiKey().isBlank()) {
            return null;
        }

        List<Map<String, Object>> userContent = new ArrayList<>();
        // 文本部分
        if (userText != null && !userText.isBlank()) {
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", userText);
            userContent.add(textPart);
        }

        // 图片部分
        if (base64Images != null) {
            for (String img : base64Images) {
                Map<String, Object> imgPart = new HashMap<>();
                imgPart.put("type", "image_url");
                Map<String, String> imgUrl = new HashMap<>();
                imgUrl.put("url", "data:image/jpeg;base64," + img);
                imgPart.put("image_url", imgUrl);
                userContent.add(imgPart);
            }
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userContent));

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProperties.getQwenVisionModel());
        payload.put("messages", messages);
        payload.put("temperature", 0.2);
        payload.put("max_tokens", 1024);

        Map<?, ?> response = qwenRestClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getQwenApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        if (response == null) return null;
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) return null;
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) return null;
        Object messageObj = firstMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) return null;
        Object content = messageMap.get("content");
        return content == null ? null : String.valueOf(content).trim();
    }
}
