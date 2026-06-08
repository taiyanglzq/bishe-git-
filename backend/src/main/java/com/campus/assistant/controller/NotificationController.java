package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.Notification;
import com.campus.assistant.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ?? ??????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @GetMapping("/page")
    public Result<Page<Notification>> page(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size,
                                           @RequestParam(required = false) Integer readStatus) {
        return Result.success(notificationMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, UserContext.getUserId())
                .eq(Notification::getDeleted, 0)
                .eq(readStatus != null, Notification::getReadStatus, readStatus)
                .orderByDesc(Notification::getCreateTime)));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, UserContext.getUserId())
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0)));
    }

    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getReceiverId().equals(UserContext.getUserId())) {
            notification.setReadStatus(1);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, UserContext.getUserId())
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0)
        ).forEach(notification -> {
            notification.setReadStatus(1);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        });
        return Result.success();
    }
}
