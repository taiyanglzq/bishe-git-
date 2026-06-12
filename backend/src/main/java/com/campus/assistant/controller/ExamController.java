package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.ExamSaveDTO;
import com.campus.assistant.entity.Exam;
import com.campus.assistant.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考试控制器，负责接收考试查询和后台管理请求并调用考试服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/exam")
public class ExamController {

    private final ExamService examService;

    @GetMapping("/page")
    public Result<Page<Exam>> page(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "10") Long size,
                                    @RequestParam(required = false) String college,
                                    @RequestParam(required = false) String examType,
                                    @RequestParam(required = false) String keyword) {
        return Result.success(examService.page(current, size, college, examType, keyword));
    }

    @GetMapping("/{id}")
    public Result<Exam> detail(@PathVariable Long id) {
        return Result.success(examService.detail(id));
    }

    @GetMapping("/manage/page")
    public Result<Page<Exam>> managePage(@RequestParam(defaultValue = "1") Long current,
                                          @RequestParam(defaultValue = "10") Long size) {
        return Result.success(examService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody ExamSaveDTO dto) {
        return Result.success(examService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ExamSaveDTO dto) {
        examService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.success();
    }
}
