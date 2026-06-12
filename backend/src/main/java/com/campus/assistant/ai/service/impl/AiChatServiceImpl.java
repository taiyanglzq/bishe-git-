package com.campus.assistant.ai.service.impl;

import com.campus.assistant.ai.dto.AiChatRequest;
import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.ai.service.AiChatService;
import com.campus.assistant.ai.utils.AiPromptUtils;
import com.campus.assistant.entity.User;
import com.campus.assistant.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI客服服务实现，支持多轮对话上下文和知识库RAG检索增强问答。
 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final DeepSeekClient deepSeekClient;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;

    @Override
    public AiChatResponse chat(AiChatRequest request, User currentUser) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        String sessionId = conversationService.getOrCreateSession(currentUser.getId(), request.getSessionId());

        // RAG检索增强
        String knowledgeContext = knowledgeService.retrieveContext(question, 3);
        String systemPrompt = AiPromptUtils.buildAssistantSystemPrompt(currentUser);
        if (knowledgeContext != null && !knowledgeContext.isBlank()) {
            systemPrompt = systemPrompt + "\n\n" + knowledgeContext;
        }

        // 构建多轮对话消息
        List<ConversationService.ChatMessage> history = conversationService.getHistory(
                currentUser.getId(), sessionId);
        StringBuilder messagesBuilder = new StringBuilder();
        if (!history.isEmpty()) {
            messagesBuilder.append("以下是之前的对话历史：\n");
            for (ConversationService.ChatMessage msg : history) {
                messagesBuilder.append(msg.role()).append(": ").append(msg.content()).append("\n");
            }
            messagesBuilder.append("请基于以上对话历史，继续回答用户的新问题。\n\n");
        }
        messagesBuilder.append("用户最新问题：").append(question);
        String fullPrompt = messagesBuilder.toString();

        String aiAnswer = deepSeekClient.chat(systemPrompt, fullPrompt);
        if (aiAnswer == null || aiAnswer.isBlank()) {
            aiAnswer = "当前AI助手暂时无法获取更详细的信息。你可以尝试从[场地预约][活动签到][校园公告][通知中心]这些模块继续查看。";
        }

        saveHistoryAfterReply(currentUser, sessionId, question, aiAnswer);
        updateSessionTitle(currentUser, sessionId, question);

        return AiChatResponse.builder()
                .intent("GENERAL")
                .answer(aiAnswer)
                .ragUsed(knowledgeContext != null && !knowledgeContext.isBlank())
                .sessionId(sessionId)
                .build();
    }

    @Override
    public List<Map<String, String>> getSessions(User currentUser) {
        return conversationService.getSessionList(currentUser.getId());
    }

    @Override
    public List<?> getHistory(User currentUser, String sessionId) {
        return conversationService.getHistory(currentUser.getId(), sessionId);
    }

    @Override
    public void deleteSession(User currentUser, String sessionId) {
        conversationService.deleteSession(currentUser.getId(), sessionId);
    }

    private void saveHistoryAfterReply(User user, String sessionId, String question, String answer) {
        if (user != null && sessionId != null) {
            conversationService.saveMessage(user.getId(), sessionId, "user", question);
            conversationService.saveMessage(user.getId(), sessionId, "assistant", answer);
        }
    }

    private void updateSessionTitle(User user, String sessionId, String question) {
        if (user != null && sessionId != null && question != null) {
            String title = question.length() > 20 ? question.substring(0, 20) + "..." : question;
            conversationService.updateTitle(user.getId(), sessionId, title);
        }
    }

}
