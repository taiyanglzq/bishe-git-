package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.*;
import com.campus.assistant.mapper.*;
import com.campus.assistant.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 个性化推荐服务实现，聚合公告、活动、场地、课程、图书和知识库推荐结果。
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final long RECOMMENDATION_TTL_MINUTES = 10L;

    private final NoticeMapper noticeMapper;
    private final ActivityMapper activityMapper;
    private final VenueMapper venueMapper;
    private final CourseMapper courseMapper;
    private final BookMapper bookMapper;
    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final CacheClient cacheClient;

    @Override
    public Map<String, Object> personal() {
        User user = UserContext.get();
        Long userId = user == null ? 0L : user.getId();
        String cacheKey = CacheKeyConstants.RECOMMENDATION + userId;
        Map<String, Object> cached = cacheClient.get(cacheKey, Map.class);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roleCode", user == null ? "GUEST" : user.getRoleCode());

        // 公告推荐（按热度）
        result.put("notices", noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getViewCount)
                .last("limit 5")));

        // 活动推荐（最新发布）
        result.put("activities", activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0)
                .eq(Activity::getStatus, 1)
                .orderByDesc(Activity::getCreateTime)
                .last("limit 5")));

        // 场地推荐
        result.put("venues", venueMapper.selectList(new LambdaQueryWrapper<Venue>()
                .eq(Venue::getDeleted, 0)
                .eq(Venue::getStatus, 1)
                .last("limit 5")));

        // 课程推荐（同院系课程）
        if (user != null && user.getCollege() != null) {
            result.put("courses", courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCollege, user.getCollege())
                    .eq(Course::getDeleted, 0)
                    .eq(Course::getStatus, 1)
                    .last("limit 5")));
        } else {
            result.put("courses", courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getDeleted, 0)
                    .eq(Course::getStatus, 1)
                    .last("limit 5")));
        }

        // 热门图书推荐
        result.put("books", bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getDeleted, 0)
                .eq(Book::getStatus, 1)
                .orderByDesc(Book::getCreateTime)
                .last("limit 5")));

        // 学习资源推荐（知识库精选）
        result.put("knowledgeEntries", knowledgeEntryMapper.selectList(new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getDeleted, 0)
                .eq(KnowledgeEntry::getStatus, 1)
                .last("limit 5")));

        result.put("reason", List.of("基于角色推荐", "基于公告热度推荐", "基于近期活动推荐",
                "基于院系课程推荐", "热门图书推荐", "校园知识库精选"));

        cacheClient.set(cacheKey, result, RECOMMENDATION_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }
}
