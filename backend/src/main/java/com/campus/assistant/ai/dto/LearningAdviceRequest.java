package com.campus.assistant.ai.dto;

import lombok.Data;

/**
 * 个性化学习建议请求
 */
@Data
public class LearningAdviceRequest {

    /**
     * 建议类型：study_plan(学习计划)、exam_prep(考试备考)、weak_subject(薄弱科目)
     */
    private String type;
}
