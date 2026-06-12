package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程保存 DTO，用于新增和编辑课程
 */
@Data
public class CourseSaveDTO {

    private Long id;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String teacherName;

    private String college;

    private String semester;

    private String classroom;

    private String scheduleInfo;

    private BigDecimal credit;

    private Integer capacity;

    private String description;

    private Integer status;
}
