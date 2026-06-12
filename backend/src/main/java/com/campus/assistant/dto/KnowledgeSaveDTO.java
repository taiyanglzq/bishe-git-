package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库条目保存 DTO
 */
@Data
public class KnowledgeSaveDTO {

    private Long id;

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String category;

    private String keywords;

    private Integer status;
}
