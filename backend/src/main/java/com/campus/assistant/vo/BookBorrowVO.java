package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书借阅记录 VO，包含借阅人信息
 */
@Data
@Builder
public class BookBorrowVO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Long userId;
    private String userRealName;
    private String userStudentNo;
    private LocalDateTime borrowTime;
    private LocalDateTime returnTime;
    private String status;
}
