package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.NoticeCommentCreateDTO;
import com.campus.assistant.dto.NoticeSaveDTO;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.entity.NoticeComment;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.NoticeCommentMapper;
import com.campus.assistant.mapper.NoticeMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.CacheEvictService;
import com.campus.assistant.service.NoticeService;
import com.campus.assistant.vo.NoticeCommentVO;
import com.campus.assistant.vo.NoticeDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公告服务实现，负责公告分页、详情、评论和后台管理逻辑。
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private static final long NOTICE_PAGE_TTL_MINUTES = 10L;
    private static final long NOTICE_DETAIL_TTL_MINUTES = 10L;

    private final NoticeMapper noticeMapper;
    private final NoticeCommentMapper noticeCommentMapper;
    private final UserMapper userMapper;
    private final CacheClient cacheClient;
    private final CacheEvictService cacheEvictService;

    @Override
    public Page<Notice> page(Long current, Long size) {
        String roleCode = currentRoleCode();
        String college = currentCollege();
        String cacheKey = CacheKeyConstants.NOTICE_PAGE + current + ":" + size + ":" + roleCode + ":" + safeCollegeSegment(college);
        Page<Notice> cached = cacheClient.get(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1);
        applyNoticeVisibility(wrapper);
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeMapper.selectPage(Page.of(current, size), wrapper);
        cacheClient.set(cacheKey, result, NOTICE_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public NoticeDetailVO detail(Long id) {
        String cacheKey = detailCacheKey(id);
        NoticeDetailVO cached = cacheClient.get(cacheKey, NoticeDetailVO.class);
        if (cached != null) {
            incrementViewCount(id);
            return cached;
        }

        Notice notice = requireVisibleNotice(id);
        incrementViewCount(notice);
        NoticeDetailVO detail = NoticeDetailVO.from(notice, queryComments(id));
        cacheClient.set(cacheKey, detail, NOTICE_DETAIL_TTL_MINUTES, TimeUnit.MINUTES);
        return detail;
    }

    @Override
    public List<NoticeCommentVO> comments(Long id) {
        requireVisibleNotice(id);
        return queryComments(id);
    }

    @Override
    public Long comment(NoticeCommentCreateDTO dto) {
        requireVisibleNotice(dto.getNoticeId());
        NoticeComment comment = new NoticeComment();
        comment.setNoticeId(dto.getNoticeId());
        comment.setUserId(UserContext.getUserId());
        comment.setContent(dto.getContent());
        comment.setDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        noticeCommentMapper.insert(comment);
        cacheEvictService.evictNoticeDetailCaches(dto.getNoticeId());
        return comment.getId();
    }

    @Override
    public Page<Notice> managePage(Long current, Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            wrapper.eq(Notice::getPublisherId, UserContext.getUserId());
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return noticeMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Long save(NoticeSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setCategory(dto.getCategory());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        notice.setPublisherId(UserContext.getUserId());
        notice.setScopeType(resolveScopeType(dto.getScopeType()));
        notice.setScopeCollege(resolveScopeCollege(dto.getScopeCollege()));
        notice.setViewCount(0L);
        notice.setDeleted(0);
        notice.setCreateTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        evictNoticeRelatedCaches(notice.getId());
        return notice.getId();
    }

    @Override
    public void update(NoticeSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Notice notice = noticeMapper.selectById(dto.getId());
        if (notice == null || notice.getDeleted() == 1) {
            throw new BusinessException(404, "公告不存在");
        }
        requireNoticeOwner(notice);
        notice.setTitle(dto.getTitle());
        notice.setCategory(dto.getCategory());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() == null ? notice.getStatus() : dto.getStatus());
        notice.setScopeType(resolveScopeType(dto.getScopeType()));
        notice.setScopeCollege(resolveScopeCollege(dto.getScopeCollege()));
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        evictNoticeRelatedCaches(notice.getId());
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Notice notice = noticeMapper.selectById(id);
        if (notice != null) {
            requireNoticeOwner(notice);
            notice.setDeleted(1);
            notice.setUpdateTime(LocalDateTime.now());
            noticeMapper.updateById(notice);
            evictNoticeRelatedCaches(id);
        }
    }

    private void applyNoticeVisibility(LambdaQueryWrapper<Notice> wrapper) {
        if (RoleUtils.hasAny("ADMIN")) {
            return;
        }
        String college = currentCollege();
        wrapper.and(query -> query.eq(Notice::getScopeType, "SCHOOL")
                .or()
                .eq(Notice::getScopeCollege, college));
    }

    private Notice requireVisibleNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || notice.getDeleted() == 1 || notice.getStatus() == 0) {
            throw new BusinessException(404, "公告不存在或不可见");
        }
        if (!RoleUtils.hasAny("ADMIN")) {
            String college = currentCollege();
            boolean schoolVisible = "SCHOOL".equals(notice.getScopeType());
            boolean collegeVisible = college != null && college.equals(notice.getScopeCollege());
            if (!schoolVisible && !collegeVisible) {
                throw new BusinessException(403, "无权查看该公告");
            }
        }
        return notice;
    }

    private List<NoticeCommentVO> queryComments(Long noticeId) {
        List<NoticeComment> comments = noticeCommentMapper.selectList(new LambdaQueryWrapper<NoticeComment>()
                .eq(NoticeComment::getNoticeId, noticeId)
                .eq(NoticeComment::getDeleted, 0)
                .orderByAsc(NoticeComment::getCreateTime));
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> userIds = comments.stream().map(NoticeComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
        return comments.stream()
                .map(comment -> NoticeCommentVO.from(comment, userMap.get(comment.getUserId())))
                .toList();
    }

    private void requireNoticeOwner(Notice notice) {
        if (RoleUtils.hasAny("TEACHER") && !notice.getPublisherId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "教师只能管理自己发布的公告");
        }
    }

    private String resolveScopeType(String scopeType) {
        if (RoleUtils.hasAny("ADMIN")) {
            return scopeType == null || scopeType.isBlank() ? "SCHOOL" : scopeType;
        }
        if ("SCHOOL".equals(scopeType)) {
            throw new BusinessException(403, "教师不能发布全校公告");
        }
        return "COLLEGE";
    }

    private String resolveScopeCollege(String scopeCollege) {
        if (RoleUtils.hasAny("ADMIN") && (scopeCollege == null || scopeCollege.isBlank())) {
            return null;
        }
        String currentCollege = currentCollege();
        String college = scopeCollege == null || scopeCollege.isBlank() ? currentCollege : scopeCollege;
        if (!RoleUtils.hasAny("ADMIN") && (college == null || !college.equals(currentCollege))) {
            throw new BusinessException(403, "教师只能发布本院系公告");
        }
        return college;
    }

    private void incrementViewCount(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice != null && notice.getDeleted() == 0) {
            incrementViewCount(notice);
        }
    }

    private void incrementViewCount(Notice notice) {
        notice.setViewCount((notice.getViewCount() == null ? 0L : notice.getViewCount()) + 1);
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
    }

    private void evictNoticeRelatedCaches(Long noticeId) {
        cacheEvictService.evictNoticePageCaches();
        cacheEvictService.evictNoticeDetailCaches(noticeId);
        cacheEvictService.evictRecommendationCaches();
        cacheEvictService.evictDashboardCaches();
    }

    private String detailCacheKey(Long noticeId) {
        Long userId = UserContext.getUserId();
        return CacheKeyConstants.NOTICE_DETAIL + noticeId + ":" + (userId == null ? 0L : userId);
    }

    private String currentRoleCode() {
        User currentUser = UserContext.get();
        return currentUser == null || currentUser.getRoleCode() == null ? "STUDENT" : currentUser.getRoleCode();
    }

    private String currentCollege() {
        User currentUser = UserContext.get();
        return currentUser == null ? null : currentUser.getCollege();
    }

    private String safeCollegeSegment(String college) {
        return college == null || college.isBlank() ? "none" : college;
    }
}
