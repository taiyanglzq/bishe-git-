package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeSaveDTO {

    private Long id;

    @NotBlank(message = "公告标题不能为空")
    private String title;

    private String category;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private String scopeType;
    private String scopeCollege;
    private Integer status;
}
