package com.campus.assistant.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI ???? ????????AI ??????????????
 */
@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = false;
    private String provider = "deepseek";
    private String apiKey;
    private String baseUrl = "https://api.deepseek.com";
    private String chatModel = "deepseek-chat";
    private boolean moderationEnabled = false;
    private boolean assistantEnabled = false;
    private int connectTimeoutMillis = 10000;
    private int readTimeoutMillis = 30000;
}
