package com.campus.assistant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiscussionPostFlagDTO {

    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;

    @NotNull(message = "状态不能为空")
    private Integer value;
}
