package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;
import com.campus.assistant.ai.enums.ModerationResult;
import com.campus.assistant.ai.service.ContentModerationService;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.DiscussionBanDTO;
import com.campus.assistant.dto.DiscussionCommentCreateDTO;
import com.campus.assistant.dto.DiscussionPostCreateDTO;
import com.campus.assistant.dto.DiscussionPostFlagDTO;
import com.campus.assistant.entity.DiscussionComment;
import com.campus.assistant.entity.DiscussionCommentLike;
import com.campus.assistant.entity.DiscussionLike;
import com.campus.assistant.entity.DiscussionPost;
import com.campus.assistant.entity.DiscussionUserBan;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.DiscussionCommentLikeMapper;
import com.campus.assistant.mapper.DiscussionCommentMapper;
import com.campus.assistant.mapper.DiscussionLikeMapper;
import com.campus.assistant.mapper.DiscussionPostMapper;
import com.campus.assistant.mapper.DiscussionUserBanMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.CacheEvictService;
import com.campus.assistant.service.DiscussionService;
import com.campus.assistant.vo.DiscussionCommentVO;
import com.campus.assistant.vo.DiscussionPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 讨论交流服务实现，负责帖子、评论、点赞、封禁和讨论区缓存逻辑。
 */
@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

    private static final long DISCUSSION_PAGE_TTL_MINUTES = 5L;

    private final DiscussionPostMapper postMapper;
    private final DiscussionCommentMapper commentMapper;
    private final DiscussionCommentLikeMapper commentLikeMapper;
    private final DiscussionLikeMapper likeMapper;
    private final DiscussionUserBanMapper banMapper;
    private final UserMapper userMapper;
    private final ContentModerationService contentModerationService;
    private final CacheClient cacheClient;
    private final CacheEvictService cacheEvictService;

    @Override
    public Page<DiscussionPostVO> page(Long current, Long size, String keyword) {
        Long userId = UserContext.getUserId();
        String cacheKey = CacheKeyConstants.DISCUSSION_PAGE + current + ":" + size + ":" + (keyword == null ? "" : keyword.trim()) + ":" + (userId == null ? "guest" : userId);
        Page<DiscussionPostVO> cached = cacheClient.get(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<DiscussionPost> wrapper = new LambdaQueryWrapper<DiscussionPost>()
                .eq(DiscussionPost::getDeleted, 0)
                .like(keyword != null && !keyword.isBlank(), DiscussionPost::getTitle, keyword)
                .orderByDesc(DiscussionPost::getPinned)
                .orderByDesc(DiscussionPost::getFeatured)
                .orderByDesc(DiscussionPost::getCreateTime);
        Page<DiscussionPost> postPage = postMapper.selectPage(Page.of(current, size), wrapper);

        List<DiscussionPost> posts = postPage.getRecords();
        Set<Long> postIds = posts.stream().map(DiscussionPost::getId).collect(Collectors.toSet());
        Set<Long> authorIds = posts.stream().map(DiscussionPost::getAuthorId).collect(Collectors.toSet());

        Map<Long, User> userMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

        Map<Long, Boolean> likedPostMap = loadLikedPostMap(postIds, userId);
        Map<Long, List<DiscussionComment>> commentMap = loadCommentMap(postIds);
        Map<Long, User> commentUserMap = loadCommentUserMap(commentMap);
        Map<Long, Long> commentLikeCountMap = loadCommentLikeCountMap(commentMap);
        Map<Long, Boolean> likedCommentMap = loadLikedCommentMap(commentMap, userId);

        Page<DiscussionPostVO> result = Page.of(current, size, postPage.getTotal());
        result.setRecords(posts.stream().map(post -> {
            List<DiscussionCommentVO> comments = commentMap.getOrDefault(post.getId(), Collections.emptyList()).stream()
                    .map(comment -> DiscussionCommentVO.from(
                            comment,
                            commentUserMap.get(comment.getUserId()),
                            commentLikeCountMap.getOrDefault(comment.getId(), 0L),
                            likedCommentMap.getOrDefault(comment.getId(), false),
                            canDeleteComment(comment)))
                    .toList();
            return DiscussionPostVO.from(
                    post,
                    userMap.get(post.getAuthorId()),
                    likedPostMap.getOrDefault(post.getId(), false),
                    canDeletePost(post),
                    canManagePost(post),
                    comments
            );
        }).toList());
        cacheClient.set(cacheKey, result, DISCUSSION_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public Long createPost(DiscussionPostCreateDTO dto) {
        User user = requireCurrentUser();
        ensureNotBanned(user.getId());
        ModerationResponse moderation = contentModerationService.moderate(ModerationRequest.builder()
                .content(dto.getTitle() + "\n" + dto.getContent())
                .scene("帖子")
                .userId(user.getId())
                .build());
        if (moderation.getResult() == ModerationResult.BLOCK) {
            throw new BusinessException(400, moderation.getSuggestion().isBlank() ? "内容包含违规信息，发布失败" : moderation.getSuggestion());
        }
        DiscussionPost post = new DiscussionPost();
        post.setAuthorId(user.getId());
        post.setCollege(user.getCollege());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImageUrl(dto.getImageUrl());
        post.setPinned(0);
        post.setFeatured(0);
        post.setLikeCount(0L);
        post.setCommentCount(0L);
        post.setDeleted(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postMapper.insert(post);
        evictDiscussionCaches();
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long comment(DiscussionCommentCreateDTO dto) {
        User user = requireCurrentUser();
        ensureNotBanned(user.getId());
        ModerationResponse moderation = contentModerationService.moderate(ModerationRequest.builder()
                .content(dto.getContent())
                .scene("评论")
                .userId(user.getId())
                .build());
        if (moderation.getResult() == ModerationResult.BLOCK) {
            throw new BusinessException(400, moderation.getSuggestion().isBlank() ? "评论包含违规信息，发布失败" : moderation.getSuggestion());
        }
        DiscussionPost post = requirePost(dto.getPostId());
        DiscussionComment comment = new DiscussionComment();
        comment.setPostId(post.getId());
        comment.setUserId(user.getId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0L);
        comment.setDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        post.setCommentCount((post.getCommentCount() == null ? 0L : post.getCommentCount()) + 1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        evictDiscussionCaches();
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long postId) {
        Long userId = requireCurrentUser().getId();
        DiscussionPost post = requirePost(postId);
        DiscussionLike like = likeMapper.selectOne(new LambdaQueryWrapper<DiscussionLike>()
                .eq(DiscussionLike::getPostId, postId)
                .eq(DiscussionLike::getUserId, userId));
        if (like == null) {
            like = new DiscussionLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setDeleted(0);
            like.setCreateTime(LocalDateTime.now());
            like.setUpdateTime(LocalDateTime.now());
            likeMapper.insert(like);
            post.setLikeCount((post.getLikeCount() == null ? 0L : post.getLikeCount()) + 1);
        } else if (like.getDeleted() == 1) {
            like.setDeleted(0);
            like.setUpdateTime(LocalDateTime.now());
            likeMapper.updateById(like);
            post.setLikeCount((post.getLikeCount() == null ? 0L : post.getLikeCount()) + 1);
        } else {
            like.setDeleted(1);
            like.setUpdateTime(LocalDateTime.now());
            likeMapper.updateById(like);
            post.setLikeCount(Math.max(0, (post.getLikeCount() == null ? 0L : post.getLikeCount()) - 1));
        }
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        evictDiscussionCaches();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        Long userId = requireCurrentUser().getId();
        DiscussionComment comment = requireComment(commentId);
        DiscussionCommentLike like = commentLikeMapper.selectOne(new LambdaQueryWrapper<DiscussionCommentLike>()
                .eq(DiscussionCommentLike::getCommentId, commentId)
                .eq(DiscussionCommentLike::getUserId, userId));
        if (like == null) {
            like = new DiscussionCommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setDeleted(0);
            like.setCreateTime(LocalDateTime.now());
            like.setUpdateTime(LocalDateTime.now());
            commentLikeMapper.insert(like);
        } else if (like.getDeleted() == 1) {
            like.setDeleted(0);
            like.setUpdateTime(LocalDateTime.now());
            commentLikeMapper.updateById(like);
        } else {
            like.setDeleted(1);
            like.setUpdateTime(LocalDateTime.now());
            commentLikeMapper.updateById(like);
        }
        comment.setLikeCount(countCommentLikes(commentId));
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        evictDiscussionCaches();
    }

    @Override
    public void deletePost(Long id) {
        DiscussionPost post = requirePost(id);
        if (!canDeletePost(post)) {
            throw new BusinessException(403, "无权删除该帖子");
        }
        post.setDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        evictDiscussionCaches();
    }

    @Override
    public void deleteComment(Long id) {
        DiscussionComment comment = commentMapper.selectById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!canDeleteComment(comment)) {
            throw new BusinessException(403, "无权删除该评论");
        }
        comment.setDeleted(1);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        evictDiscussionCaches();
    }

    @Override
    public void pin(DiscussionPostFlagDTO dto) {
        DiscussionPost post = requirePost(dto.getPostId());
        requireManagePost(post);
        post.setPinned(normalizeFlag(dto.getValue()));
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        evictDiscussionCaches();
    }

    @Override
    public void feature(DiscussionPostFlagDTO dto) {
        DiscussionPost post = requirePost(dto.getPostId());
        requireManagePost(post);
        post.setFeatured(normalizeFlag(dto.getValue()));
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        evictDiscussionCaches();
    }

    @Override
    public void ban(DiscussionBanDTO dto) {
        RoleUtils.requireAny("ADMIN");
        DiscussionUserBan ban = banMapper.selectOne(new LambdaQueryWrapper<DiscussionUserBan>()
                .eq(DiscussionUserBan::getUserId, dto.getUserId()));
        if (ban == null) {
            ban = new DiscussionUserBan();
            ban.setUserId(dto.getUserId());
            ban.setOperatorId(UserContext.getUserId());
            ban.setReason(dto.getReason());
            ban.setStatus(1);
            ban.setCreateTime(LocalDateTime.now());
            ban.setUpdateTime(LocalDateTime.now());
            banMapper.insert(ban);
        } else {
            ban.setOperatorId(UserContext.getUserId());
            ban.setReason(dto.getReason());
            ban.setStatus(1);
            ban.setUpdateTime(LocalDateTime.now());
            banMapper.updateById(ban);
        }
        evictDiscussionCaches();
    }

    @Override
    public void unban(Long userId) {
        RoleUtils.requireAny("ADMIN");
        DiscussionUserBan ban = banMapper.selectOne(new LambdaQueryWrapper<DiscussionUserBan>()
                .eq(DiscussionUserBan::getUserId, userId));
        if (ban != null) {
            ban.setStatus(0);
            ban.setUpdateTime(LocalDateTime.now());
            banMapper.updateById(ban);
            evictDiscussionCaches();
        }
    }

    private User requireCurrentUser() {
        User user = UserContext.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    private DiscussionPost requirePost(Long id) {
        DiscussionPost post = postMapper.selectById(id);
        if (post == null || post.getDeleted() == 1) {
            throw new BusinessException(404, "帖子不存在");
        }
        return post;
    }

    private DiscussionComment requireComment(Long id) {
        DiscussionComment comment = commentMapper.selectById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException(404, "评论不存在");
        }
        return comment;
    }

    private void ensureNotBanned(Long userId) {
        Long count = banMapper.selectCount(new LambdaQueryWrapper<DiscussionUserBan>()
                .eq(DiscussionUserBan::getUserId, userId)
                .eq(DiscussionUserBan::getStatus, 1));
        if (count > 0) {
            throw new BusinessException(403, "你已被限制发帖和评论，请联系管理员");
        }
    }

    private boolean canDeletePost(DiscussionPost post) {
        Long userId = UserContext.getUserId();
        return RoleUtils.hasAny("ADMIN") || post.getAuthorId().equals(userId) || canManagePost(post);
    }

    private boolean canDeleteComment(DiscussionComment comment) {
        Long userId = UserContext.getUserId();
        DiscussionPost post = postMapper.selectById(comment.getPostId());
        return RoleUtils.hasAny("ADMIN") || comment.getUserId().equals(userId) || (post != null && canManagePost(post));
    }

    private void requireManagePost(DiscussionPost post) {
        if (!canManagePost(post)) {
            throw new BusinessException(403, "无权管理该帖子");
        }
    }

    private boolean canManagePost(DiscussionPost post) {
        if (RoleUtils.hasAny("ADMIN")) {
            return true;
        }
        User user = UserContext.get();
        return RoleUtils.hasAny("TEACHER")
                && user != null
                && user.getCollege() != null
                && user.getCollege().equals(post.getCollege());
    }

    private Integer normalizeFlag(Integer value) {
        return value != null && value == 1 ? 1 : 0;
    }

    private long countCommentLikes(Long commentId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<DiscussionCommentLike>()
                .eq(DiscussionCommentLike::getCommentId, commentId)
                .eq(DiscussionCommentLike::getDeleted, 0));
    }

    private Map<Long, Boolean> loadLikedPostMap(Set<Long> postIds, Long userId) {
        if (userId == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return likeMapper.selectList(new LambdaQueryWrapper<DiscussionLike>()
                        .eq(DiscussionLike::getUserId, userId)
                        .eq(DiscussionLike::getDeleted, 0)
                        .in(DiscussionLike::getPostId, postIds))
                .stream()
                .collect(Collectors.toMap(DiscussionLike::getPostId, item -> true, (left, right) -> left));
    }

    private Map<Long, List<DiscussionComment>> loadCommentMap(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentMapper.selectList(new LambdaQueryWrapper<DiscussionComment>()
                        .in(DiscussionComment::getPostId, postIds)
                        .eq(DiscussionComment::getDeleted, 0)
                        .orderByAsc(DiscussionComment::getCreateTime))
                .stream()
                .collect(Collectors.groupingBy(DiscussionComment::getPostId));
    }

    private Map<Long, User> loadCommentUserMap(Map<Long, List<DiscussionComment>> commentMap) {
        Set<Long> userIds = commentMap.values().stream()
                .flatMap(List::stream)
                .map(DiscussionComment::getUserId)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Long> loadCommentLikeCountMap(Map<Long, List<DiscussionComment>> commentMap) {
        Set<Long> commentIds = commentMap.values().stream()
                .flatMap(List::stream)
                .map(DiscussionComment::getId)
                .collect(Collectors.toSet());
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<DiscussionCommentLike>()
                        .in(DiscussionCommentLike::getCommentId, commentIds)
                        .eq(DiscussionCommentLike::getDeleted, 0))
                .stream()
                .collect(Collectors.groupingBy(DiscussionCommentLike::getCommentId, Collectors.counting()));
    }

    private Map<Long, Boolean> loadLikedCommentMap(Map<Long, List<DiscussionComment>> commentMap, Long userId) {
        Set<Long> commentIds = commentMap.values().stream()
                .flatMap(List::stream)
                .map(DiscussionComment::getId)
                .collect(Collectors.toSet());
        if (userId == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<DiscussionCommentLike>()
                        .eq(DiscussionCommentLike::getUserId, userId)
                        .eq(DiscussionCommentLike::getDeleted, 0)
                        .in(DiscussionCommentLike::getCommentId, commentIds))
                .stream()
                .collect(Collectors.toMap(DiscussionCommentLike::getCommentId, item -> true, (left, right) -> left));
    }

    private void evictDiscussionCaches() {
        cacheEvictService.evictDiscussionCaches();
        cacheEvictService.evictDashboardCaches();
    }
}
