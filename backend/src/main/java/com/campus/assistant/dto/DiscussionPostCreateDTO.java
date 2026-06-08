package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ???? DTO???????????????????
 */
@Data
public class DiscussionPostCreateDTO {

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题不能超过 100 字")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "帖子内容不能超过 2000 字")
    private String content;

    private String imageUrl;
}
