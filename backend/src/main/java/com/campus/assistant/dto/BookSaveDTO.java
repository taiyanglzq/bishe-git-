package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图书保存 DTO，用于新增和编辑图书信息
 */
@Data
public class BookSaveDTO {

    private Long id;

    @NotBlank(message = "书名不能为空")
    private String title;

    private String author;

    private String isbn;

    private String publisher;

    private String publishYear;

    private String category;

    private String location;

    private Integer totalCount;

    private Integer availableCount;

    private String description;

    private String coverUrl;

    private Integer status;
}
