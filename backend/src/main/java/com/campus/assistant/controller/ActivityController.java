package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.ActivityEnrollDTO;
import com.campus.assistant.dto.ActivitySaveDTO;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.service.ActivityBizService;
import com.campus.assistant.vo.ActivityRecordVO;
import com.campus.assistant.vo.ActivityVO;
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
 * 活动控制器，负责接收活动展示、报名、签到和后台管理请求并调用活动服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {

    private final ActivityBizService activityBizService;

    @GetMapping("/page")
    public Result<Page<ActivityVO>> page(@RequestParam(defaultValue = "1") Long current,
                                         @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.page(current, size));
    }

    @PostMapping("/enroll")
    public Result<Void> enroll(@Valid @RequestBody ActivityEnrollDTO dto) {
        activityBizService.enroll(dto);
        return Result.success();
    }

    @PostMapping("/cancel/{activityId}")
    public Result<Void> cancel(@PathVariable Long activityId) {
        activityBizService.cancelEnroll(activityId);
        return Result.success();
    }

    @GetMapping("/my-enrollments")
    public Result<Page<ActivityRecordVO>> myEnrollments(@RequestParam(defaultValue = "1") Long current,
                                                        @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.myEnrollments(current, size));
    }

    @GetMapping("/my-checkins")
    public Result<Page<ActivityRecordVO>> myCheckins(@RequestParam(defaultValue = "1") Long current,
                                                     @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.myCheckins(current, size));
    }

    @GetMapping("/manage/page")
    public Result<Page<Activity>> managePage(@RequestParam(defaultValue = "1") Long current,
                                             @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody ActivitySaveDTO dto) {
        return Result.success(activityBizService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ActivitySaveDTO dto) {
        activityBizService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activityBizService.delete(id);
        return Result.success();
    }
}
