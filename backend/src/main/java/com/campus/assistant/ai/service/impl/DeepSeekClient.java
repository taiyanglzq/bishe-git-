package com.campus.assistant.ai.service.impl;

import com.campus.assistant.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek ???? ?????????DeepSeek ???????????
 */
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private final RestClient aiRestClient;
    private final AiProperties aiProperties;

    public String chat(String systemPrompt, String userPrompt) {
        if (!aiProperties.isEnabled() || aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            return null;
        }
        Map<String, Object> payload = Map.of(
                "model", aiProperties.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );
        Map<?, ?> response = aiRestClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);
        if (response == null) {
            return null;
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            return null;
        }
        Object messageObj = firstMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            return null;
        }
        Object content = messageMap.get("content");
        return content == null ? null : String.valueOf(content).trim();
    }
}
