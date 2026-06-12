package com.campus.assistant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 多模态对话请求，支持文本+图片输入
 */
@Data
public class MultimodalChatRequest {

    /**
     * 文本问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 会话ID（可选）
     */
    private String sessionId;

    /**
     * Base64 编码的图片列表（可选）
     */
    private List<String> images;
}
