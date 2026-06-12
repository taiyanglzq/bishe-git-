package com.campus.assistant.ai.controller;

import com.campus.assistant.ai.dto.AiChatRequest;
import com.campus.assistant.ai.dto.MultimodalChatRequest;
import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.ai.dto.LearningAdviceRequest;
import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;
import com.campus.assistant.ai.service.AiChatService;
import com.campus.assistant.ai.service.ContentModerationService;
import com.campus.assistant.ai.service.impl.DeepSeekClient;
import com.campus.assistant.ai.service.impl.MultimodalClient;
import com.campus.assistant.ai.utils.AiPromptUtils;
import com.campus.assistant.ai.utils.LearningAdviceBuilder;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 对话控制器，支持多轮对话会话管理和个性化学习建议。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final ContentModerationService contentModerationService;
    private final LearningAdviceBuilder learningAdviceBuilder;
    private final DeepSeekClient deepSeekClient;
    private final MultimodalClient multimodalClient;

    /**
     * 发送消息并获取回复，支持多轮对话（通过 sessionId 关联上下文）
     */
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        User currentUser = UserContext.get();
        return Result.success(aiChatService.chat(request, currentUser));
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/sessions")
    public Result<List<Map<String, String>>> sessions() {
        User currentUser = UserContext.get();
        List<Map<String, String>> list = aiChatService.getSessions(currentUser);
        return Result.success(list);
    }

    /**
     * 获取指定会话的历史消息
     */
    @GetMapping("/sessions/{sessionId}/history")
    public Result<List<?>> history(@PathVariable String sessionId) {
        User currentUser = UserContext.get();
        return Result.success(aiChatService.getHistory(currentUser, sessionId));
    }

    /**
     * 删除指定会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        User currentUser = UserContext.get();
        aiChatService.deleteSession(currentUser, sessionId);
        return Result.success();
    }

    /**
     * 获取个性化学习建议
     */
    @PostMapping("/learning-advice")
    public Result<AiChatResponse> learningAdvice(@RequestBody LearningAdviceRequest request) {
        User currentUser = UserContext.get();
        String prompt = learningAdviceBuilder.buildAdvicePrompt(currentUser, request.getType());
        String answer = deepSeekClient.chat(prompt, "");
        if (answer == null || answer.isBlank()) {
            answer = "当前无法生成学习建议，请确保课程和考试数据已录入，稍后再试。";
        }
        return Result.success(AiChatResponse.builder()
                .intent("LEARNING_ADVICE")
                .answer(answer)
                .ragUsed(false)
                .build());
    }

    /**
     * 多模态对话（支持文本+图片）
     */
    @PostMapping("/chat/multimodal")
    public Result<AiChatResponse> multimodalChat(@Valid @RequestBody MultimodalChatRequest request) {
        User currentUser = UserContext.get();
        String systemPrompt = AiPromptUtils.buildAssistantSystemPrompt(currentUser);
        String answer = multimodalClient.chat(systemPrompt, request.getQuestion(), request.getImages());
        if (answer == null || answer.isBlank()) {
            answer = "多模态AI助手暂不可用。请确保已配置AI Key，且模型支持视觉输入。";
        }
        return Result.success(AiChatResponse.builder()
                .intent("MULTIMODAL")
                .answer(answer)
                .ragUsed(false)
                .sessionId(request.getSessionId())
                .build());
    }

    /**
     * 内容审核预览
     */
    @PostMapping("/moderate/preview")
    public Result<ModerationResponse> moderatePreview(@Valid @RequestBody ModerationRequest request) {
        ModerationRequest previewRequest = ModerationRequest.builder()
                .content(request.getContent())
                .scene(request.getScene())
                .userId(UserContext.getUserId())
                .build();
        return Result.success(contentModerationService.moderate(previewRequest));
    }
}
