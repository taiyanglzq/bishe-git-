package com.campus.assistant.ai.service.impl;

import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;
import com.campus.assistant.ai.enums.ModerationResult;
import com.campus.assistant.ai.service.ContentModerationService;
import com.campus.assistant.ai.utils.AiPromptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ???? ????????????????????
 */
@Service
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private static final List<String> BLOCKED_WORDS = List.of("黄色", "暴力", "代考", "兼职加微信", "出售试题");
    private static final List<String> FLAGGED_WORDS = List.of("傻", "滚", "垃圾", "妈的");

    private final DeepSeekClient deepSeekClient;

    @Override
    public ModerationResponse moderate(ModerationRequest request) {
        String content = request.getContent() == null ? "" : request.getContent().trim();
        for (String word : BLOCKED_WORDS) {
            if (content.contains(word)) {
                return ModerationResponse.builder()
                        .result(ModerationResult.BLOCK)
                        .reason("命中系统违禁词：" + word)
                        .suggestion("请删除违规内容后重新提交")
                        .build();
            }
        }
        for (String word : FLAGGED_WORDS) {
            if (content.contains(word)) {
                return ModerationResponse.builder()
                        .result(ModerationResult.FLAG)
                        .reason("内容包含不文明表达：" + word)
                        .suggestion("建议修改用词，保持文明交流")
                        .build();
            }
        }
        String aiResult = deepSeekClient.chat(
                "你只能输出 PASS、FLAG 或 BLOCK 其中一个单词。",
                AiPromptUtils.buildModerationPrompt(request.getScene(), content)
        );
        if ("BLOCK".equalsIgnoreCase(aiResult)) {
            return ModerationResponse.builder()
                    .result(ModerationResult.BLOCK)
                    .reason("AI 审核判定为违规内容")
                    .suggestion("请修改内容后重新提交")
                    .build();
        }
        if ("FLAG".equalsIgnoreCase(aiResult)) {
            return ModerationResponse.builder()
                    .result(ModerationResult.FLAG)
                    .reason("AI 审核判定存在风险表达")
                    .suggestion("建议优化措辞后再发布")
                    .build();
        }
        return ModerationResponse.builder()
                .result(ModerationResult.PASS)
                .reason("审核通过")
                .suggestion("")
                .build();
    }
}
