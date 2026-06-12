package com.campus.assistant.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.ai.dto.AiChatRequest;
import com.campus.assistant.ai.dto.AiChatResponse;
import com.campus.assistant.ai.service.AiChatService;
import com.campus.assistant.ai.utils.AiPromptUtils;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.ActivityEnroll;
import com.campus.assistant.entity.Booking;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.ActivityEnrollMapper;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.BookingMapper;
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
    private final BookingMapper bookingMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final ActivityMapper activityMapper;
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;

    @Override
    public AiChatResponse chat(AiChatRequest request, User currentUser) {
        String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
        String sessionId = conversationService.getOrCreateSession(currentUser.getId(), request.getSessionId());

        // 业务意图识别：优先处理预约和活动状态查询
        if (currentUser != null && isBookingStatusQuestion(question)) {
            AiChatResponse resp = bookingStatusReply(currentUser);
            saveHistoryAfterReply(currentUser, sessionId, question, resp.getAnswer());
            resp.setSessionId(sessionId);
            return resp;
        }
        if (currentUser != null && isActivityStatusQuestion(question)) {
            AiChatResponse resp = activityStatusReply(currentUser);
            saveHistoryAfterReply(currentUser, sessionId, question, resp.getAnswer());
            resp.setSessionId(sessionId);
            return resp;
        }
        if (isBookingGuideQuestion(question)) {
            String answer = "你可以进入[场地预约]页面，先选择场地和日期，再选择时间段并填写预约原因，最后提交预约申请。提交后教师或管理员会进行审核。";
            saveHistoryAfterReply(currentUser, sessionId, question, answer);
            return AiChatResponse.builder()
                    .intent("BOOKING_GUIDE")
                    .suggestedAction("/booking")
                    .answer(answer)
                    .sessionId(sessionId)
                    .build();
        }
        if (question.contains("图书馆几点关门")) {
            String answer = "根据校园知识库，图书馆开放时间为周一至周日 8:00-22:00，自习区24小时开放。节假日开放时间另行通知。";
            saveHistoryAfterReply(currentUser, sessionId, question, answer);
            return AiChatResponse.builder()
                    .intent("FAQ")
                    .answer(answer)
                    .ragUsed(true)
                    .sessionId(sessionId)
                    .build();
        }

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

    private boolean isBookingStatusQuestion(String question) {
        return question.contains("预约")
                && (question.contains("批了没") || question.contains("通过了没")
                || question.contains("审核") || question.contains("状态"));
    }

    private boolean isActivityStatusQuestion(String question) {
        return (question.contains("报名") || question.contains("活动"))
                && (question.contains("状态") || question.contains("成功")
                || question.contains("记录") || question.contains("报了没"));
    }

    private boolean isBookingGuideQuestion(String question) {
        return question.contains("怎么借篮球场") || question.contains("怎么预约场地")
                || question.contains("预约场地");
    }

    private AiChatResponse bookingStatusReply(User currentUser) {
        Booking latest = bookingMapper.selectOne(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStudentId, currentUser.getId())
                .eq(Booking::getDeleted, 0)
                .orderByDesc(Booking::getCreateTime)
                .last("limit 1"));
        if (latest == null) {
            return AiChatResponse.builder()
                    .intent("BOOKING_STATUS")
                    .suggestedAction("/booking")
                    .answer("你当前还没有场地预约记录，可以前往[场地预约]页面发起申请。")
                    .build();
        }
        String statusText = switch (latest.getStatus()) {
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已驳回";
            case "CANCELLED" -> "已取消";
            default -> "待审核";
        };
        return AiChatResponse.builder()
                .intent("BOOKING_STATUS")
                .suggestedAction("/booking")
                .answer("你最近一条场地预约当前状态为[" + statusText + "]，预约日期是 " + latest.getBookingDate() + "，时间段是 " + latest.getTimeRange() + "。")
                .build();
    }

    private AiChatResponse activityStatusReply(User currentUser) {
        ActivityEnroll latest = activityEnrollMapper.selectOne(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getStudentId, currentUser.getId())
                .eq(ActivityEnroll::getDeleted, 0)
                .orderByDesc(ActivityEnroll::getCreateTime)
                .last("limit 1"));
        if (latest == null) {
            return AiChatResponse.builder()
                    .intent("ACTIVITY_STATUS")
                    .suggestedAction("/activity")
                    .answer("你当前还没有活动报名记录，可以前往[活动签到]页面查看可报名活动。")
                    .build();
        }
        Activity activity = activityMapper.selectById(latest.getActivityId());
        String title = activity == null ? "未知活动" : activity.getTitle();
        String statusText = switch (String.valueOf(latest.getStatus())) {
            case "ENROLLED" -> "已报名";
            case "CANCELLED" -> "已取消报名";
            default -> String.valueOf(latest.getStatus());
        };
        return AiChatResponse.builder()
                .intent("ACTIVITY_STATUS")
                .suggestedAction("/activity")
                .answer("你最近一条活动报名记录是[" + title + "]，当前状态为[" + statusText + "]。可以前往[活动签到]页面继续查看。")
                .build();
    }
}
