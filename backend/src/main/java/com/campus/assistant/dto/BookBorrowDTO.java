package com.campus.assistant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 图书借阅请求 DTO
 */
@Data
public class BookBorrowDTO {

    @NotNull(message = "图书不能为空")
    private Long bookId;
}
