package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.Notification;
import com.campus.assistant.mapper.NotificationMapper;
import com.campus.assistant.service.CacheEvictService;
import com.campus.assistant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 通知服务实现，负责通知发送、通知分页、未读统计和已读状态更新逻辑。
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final long NOTIFICATION_PAGE_TTL_MINUTES = 2L;
    private static final long NOTIFICATION_UNREAD_TTL_MINUTES = 2L;

    private final NotificationMapper notificationMapper;
    private final CacheClient cacheClient;
    private final CacheEvictService cacheEvictService;

    @Override
    public void send(Long receiverId, String title, String content, String bizType, Long bizId) {
        if (receiverId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setBizType(bizType);
        notification.setBizId(bizId);
        notification.setReadStatus(0);
        notification.setDeleted(0);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        evictNotificationCaches(receiverId);
    }

    @Override
    public Page<Notification> page(Long current, Long size, Integer readStatus) {
        Long userId = UserContext.getUserId();
        String cacheKey = CacheKeyConstants.NOTIFICATION_PAGE + userId + ":" + current + ":" + size + ":" + (readStatus == null ? "all" : readStatus);
        Page<Notification> cached = cacheClient.get(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        Page<Notification> result = notificationMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getDeleted, 0)
                .eq(readStatus != null, Notification::getReadStatus, readStatus)
                .orderByDesc(Notification::getCreateTime));
        cacheClient.set(cacheKey, result, NOTIFICATION_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public Long unreadCount() {
        Long userId = UserContext.getUserId();
        String cacheKey = CacheKeyConstants.NOTIFICATION_UNREAD_COUNT + userId;
        String cached = cacheClient.getRaw(cacheKey);
        if (cached != null) {
            return Long.parseLong(cached);
        }

        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0));
        cacheClient.setRaw(cacheKey, String.valueOf(count), NOTIFICATION_UNREAD_TTL_MINUTES, TimeUnit.MINUTES);
        return count;
    }

    @Override
    public void read(Long id) {
        Long userId = UserContext.getUserId();
        Notification notification = notificationMapper.selectById(id);
        if (notification != null && notification.getReceiverId().equals(userId)) {
            notification.setReadStatus(1);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
            evictNotificationCaches(userId);
        }
    }

    @Override
    public void readAll() {
        Long userId = UserContext.getUserId();
        notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0)
        ).forEach(notification -> {
            notification.setReadStatus(1);
            notification.setUpdateTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        });
        evictNotificationCaches(userId);
    }

    private void evictNotificationCaches(Long userId) {
        cacheEvictService.evictNotificationCaches(userId);
        cacheEvictService.evictUserDashboardCaches(userId);
    }
}
