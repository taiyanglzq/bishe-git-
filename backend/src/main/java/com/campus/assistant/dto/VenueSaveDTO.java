package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ?? DTO?????????????????
 */
@Data
public class VenueSaveDTO {

    private Long id;

    @NotBlank(message = "场地名称不能为空")
    private String name;

    private String location;
    private String imageUrl;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    private Integer status;
}
