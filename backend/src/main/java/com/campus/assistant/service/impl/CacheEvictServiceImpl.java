package com.campus.assistant.service.impl;

import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.service.CacheEvictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 缓存失效服务实现，负责统一清理首页、公告、活动、推荐、通知和讨论区相关缓存。
 */
@Service
@RequiredArgsConstructor
public class CacheEvictServiceImpl implements CacheEvictService {

    private final CacheClient cacheClient;

    @Override
    public void evictDashboardCaches() {
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.DASHBOARD_STATS + "*"));
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.DASHBOARD_WORKBENCH + "*"));
    }

    @Override
    public void evictRecommendationCaches() {
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.RECOMMENDATION + "*"));
    }

    @Override
    public void evictNoticePageCaches() {
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.NOTICE_PAGE + "*"));
    }

    @Override
    public void evictNoticeDetailCaches(Long noticeId) {
        if (noticeId == null) {
            return;
        }
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.NOTICE_DETAIL + noticeId + ":*"));
    }

    @Override
    public void evictNotificationCaches(Long userId) {
        if (userId == null) {
            return;
        }
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.NOTIFICATION_PAGE + userId + ":*"));
        cacheClient.delete(CacheKeyConstants.NOTIFICATION_UNREAD_COUNT + userId);
    }

    @Override
    public void evictDiscussionCaches() {
        cacheClient.delete(cacheClient.scan(CacheKeyConstants.DISCUSSION_PAGE + "*"));
    }
}
