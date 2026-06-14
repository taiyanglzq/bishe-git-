package com.campus.assistant.ai.utils;

import com.campus.assistant.entity.User;

public final class AiPromptUtils {

    private AiPromptUtils() {
    }

    public static String buildAssistantSystemPrompt(User currentUser) {
        String role = currentUser == null ? "游客" : roleName(currentUser.getRoleCode());
        String name = currentUser == null ? "用户" : currentUser.getRealName();
        return """
                你是智慧校园助手的AI客服。
                你的职责：
                1. 回答系统使用问题，如课程查询、考试安排、图书借阅、校园公告、通知中心、讨论交流。
                2. 优先给出简洁、明确、可执行的步骤。
                3. 若用户询问个人状态，可结合提供的上下文信息进行回答。
                4. 不编造学校未提供的规章制度；不确定时明确说明。
                5. 输出中文，避免技术黑话。
                当前用户：%s（角色：%s）
                """.formatted(name, role);
    }

    public static String buildModerationPrompt(String scene, String content) {
        return """
                你是校园讨论区内容审核员。
                请审核以下%s内容是否存在违规风险，包括政治敏感、色情低俗、暴力恐吓、辱骂攻击、广告引流、违法信息。
                你只能返回以下三种结论之一：
                PASS
                FLAG
                BLOCK
                如果内容基本正常但有轻微风险或明显错别字，可判定为 FLAG。
                如果内容明显违规，判定为 BLOCK。
                内容：
                %s
                """.formatted(scene == null ? "讨论" : scene, content);
    }

    private static String roleName(String roleCode) {
        return switch (roleCode) {
            case "ADMIN" -> "管理员";
            case "TEACHER" -> "教师";
            default -> "学生";
        };
    }
}
