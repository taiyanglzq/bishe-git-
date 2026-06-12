package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.entity.Notification;
import com.campus.assistant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知控制器，负责接收通知中心相关请求并调用通知服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/page")
    public Result<Page<Notification>> page(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size,
                                           @RequestParam(required = false) Integer readStatus) {
        return Result.success(notificationService.page(current, size, readStatus));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.unreadCount());
    }

    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.read(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        notificationService.readAll();
        return Result.success();
    }
}
