package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考试保存 DTO，用于新增和编辑考试安排
 */
@Data
public class ExamSaveDTO {

    private Long id;

    private Long courseId;

    @NotBlank(message = "考试科目名称不能为空")
    private String courseName;

    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    private String location;

    private String seatNo;

    @NotBlank(message = "考试类型不能为空")
    private String examType;

    private String college;

    private String invigilator;

    private Integer status;
}
