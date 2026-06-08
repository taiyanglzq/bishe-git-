package com.campus.assistant.ai.controller;

import com.campus.assistant.ai.dto.AiChatRequest;
import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;
import com.campus.assistant.ai.service.AiChatService;
import com.campus.assistant.ai.service.ContentModerationService;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI ?? ?????????????AI ?????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final ContentModerationService contentModerationService;

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(request.getQuestion(), UserContext.get()));
    }

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
