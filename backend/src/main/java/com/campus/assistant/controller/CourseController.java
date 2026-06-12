package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.CourseSaveDTO;
import com.campus.assistant.entity.Course;
import com.campus.assistant.service.CourseService;
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
 * 课程控制器，负责接收课程查询和后台管理请求并调用课程服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/page")
    public Result<Page<Course>> page(@RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size,
                                      @RequestParam(required = false) String college,
                                      @RequestParam(required = false) String semester,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(courseService.page(current, size, college, semester, keyword));
    }

    @GetMapping("/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    @GetMapping("/manage/page")
    public Result<Page<Course>> managePage(@RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size) {
        return Result.success(courseService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody CourseSaveDTO dto) {
        return Result.success(courseService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody CourseSaveDTO dto) {
        courseService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }
}
