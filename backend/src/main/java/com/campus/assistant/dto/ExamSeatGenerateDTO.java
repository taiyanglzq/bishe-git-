package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 考试座位生成请求 DTO
 */
@Data
public class ExamSeatGenerateDTO {

    /**
     * 生成模式：
     * CLASSROOM — 按教室行列（A1-A10, B1-B10...）
     * STUDENT_NO — 按学号排序
     * RANDOM — 随机
     */
    @NotBlank(message = "生成模式不能为空")
    private String mode;
}
