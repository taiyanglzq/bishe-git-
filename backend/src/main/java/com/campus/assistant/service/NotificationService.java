package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.entity.Notification;

/**
 * 通知服务接口，定义通知发送、分页查询、未读统计和已读处理等业务能力。
 */
public interface NotificationService {

    void send(Long receiverId, String title, String content, String bizType, Long bizId);

    Page<Notification> page(Long current, Long size, Integer readStatus);

    Long unreadCount();

    void read(Long id);

    void readAll();
}
