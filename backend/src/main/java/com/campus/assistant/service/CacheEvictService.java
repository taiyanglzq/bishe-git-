package com.campus.assistant.service;

/**
 * 缓存失效服务接口，负责统一管理业务写操作后的缓存删除逻辑。
 */
public interface CacheEvictService {

    void evictDashboardCaches();

    void evictUserDashboardCaches(Long userId);

    void evictRecommendationCaches();

    void evictNoticePageCaches();

    void evictNoticeDetailCaches(Long noticeId);

    void evictNotificationCaches(Long userId);

    void evictDiscussionCaches();
}
