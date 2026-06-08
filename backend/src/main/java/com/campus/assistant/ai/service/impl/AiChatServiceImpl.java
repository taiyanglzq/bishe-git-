package com.campus.assistant.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI ?? ?????????AI ?????????
 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final DeepSeekClient deepSeekClient;
    private final BookingMapper bookingMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final ActivityMapper activityMapper;

    @Override
    public AiChatResponse chat(String question, User currentUser) {
        String lowered = question == null ? "" : question.trim();
        if (currentUser != null && isBookingStatusQuestion(lowered)) {
            return bookingStatusReply(currentUser);
        }
        if (currentUser != null && isActivityStatusQuestion(lowered)) {
            return activityStatusReply(currentUser);
        }
        if (isBookingGuideQuestion(lowered)) {
            return AiChatResponse.builder()
                    .intent("BOOKING_GUIDE")
                    .suggestedAction("/booking")
                    .answer("你可以进入“场地预约”页面，先选择场地和日期，再选择时间段并填写预约原因，最后提交预约申请。提交后教师或管理员会进行审核。")
                    .build();
        }
        if (lowered.contains("图书馆几点关门")) {
            return AiChatResponse.builder()
                    .intent("FAQ")
                    .answer("当前系统内没有接入图书馆营业时间数据，建议查看学校官方公告、图书馆主页或咨询值班老师。")
                    .build();
        }
        String aiAnswer = deepSeekClient.chat(
                AiPromptUtils.buildAssistantSystemPrompt(currentUser),
                lowered
        );
        if (aiAnswer == null || aiAnswer.isBlank()) {
            aiAnswer = "当前 AI 助手暂时无法获取更详细的信息。你可以尝试从“场地预约”“活动签到”“校园公告”“通知中心”这些模块继续查看。";
        }
        return AiChatResponse.builder()
                .intent("GENERAL")
                .answer(aiAnswer)
                .build();
    }

    private boolean isBookingStatusQuestion(String question) {
        return question.contains("预约")
                && (question.contains("老师批了没")
                || question.contains("通过了没")
                || question.contains("审核")
                || question.contains("状态"));
    }

    private boolean isActivityStatusQuestion(String question) {
        return (question.contains("报名") || question.contains("活动"))
                && (question.contains("状态")
                || question.contains("成功")
                || question.contains("记录")
                || question.contains("报了没"));
    }

    private boolean isBookingGuideQuestion(String question) {
        return question.contains("怎么借篮球场")
                || question.contains("怎么预约场地")
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
                    .answer("你当前还没有场地预约记录，可以前往“场地预约”页面发起申请。")
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
                .answer("你最近一条场地预约当前状态为“%s”，预约日期是 %s，时间段是 %s。".formatted(
                        statusText,
                        latest.getBookingDate(),
                        latest.getTimeRange()))
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
                    .answer("你当前还没有活动报名记录，可以前往“活动签到”页面查看可报名活动。")
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
                .answer("你最近一条活动报名记录是“%s”，当前状态为“%s”。可以前往“活动签到”页面继续查看报名或签到情况。".formatted(
                        title,
                        statusText))
                .build();
    }
}
