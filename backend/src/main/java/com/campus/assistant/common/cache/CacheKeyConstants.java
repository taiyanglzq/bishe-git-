package com.campus.assistant.common.cache;

/**
 * Redis 缓存 Key 常量类，用于统一维护项目中的业务缓存前缀。
 */
public final class CacheKeyConstants {

    private CacheKeyConstants() {
    }

    public static final String CACHE_NULL_VALUE = "__NULL__";

    public static final String USER_INFO = "campus:user:info:";

    public static final String DASHBOARD_STATS = "campus:dashboard:stats:";
    public static final String DASHBOARD_WORKBENCH = "campus:dashboard:workbench:";

    public static final String NOTICE_PAGE = "campus:notice:page:";
    public static final String NOTICE_DETAIL = "campus:notice:detail:";

    public static final String NOTIFICATION_PAGE = "campus:notification:page:";
    public static final String NOTIFICATION_UNREAD_COUNT = "campus:notification:unread:";

    public static final String VENUE_PAGE = "campus:venue:page:";
    public static final String VENUE_DETAIL = "campus:venue:detail:";
    public static final String VENUE_SLOT = "campus:venue:slot:";

    public static final String BOOKING_PAGE = "campus:booking:page:";
    public static final String BOOKING_AUDIT = "campus:booking:audit:";

    public static final String ACTIVITY_PAGE = "campus:activity:page:";
    public static final String ACTIVITY_DETAIL = "campus:activity:detail:";
    public static final String ACTIVITY_ENROLL = "campus:activity:enroll:";
    public static final String ACTIVITY_CHECKIN = "campus:activity:checkin:";

    public static final String RECOMMENDATION = "campus:recommend:";

    public static final String DISCUSSION_PAGE = "campus:discussion:page:";
    public static final String DISCUSSION_DETAIL = "campus:discussion:detail:";
}
