package com.campus.assistant.controller;

import com.campus.assistant.ai.dto.ModerationRequest;
import com.campus.assistant.ai.dto.ModerationResponse;
import com.campus.assistant.ai.enums.ModerationResult;
import com.campus.assistant.ai.service.ContentModerationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.result.Result;
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
import com.campus.assistant.vo.DiscussionCommentVO;
import com.campus.assistant.vo.DiscussionPostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ???? ????????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/discussion")
public class DiscussionController {

    private final DiscussionPostMapper postMapper;
    private final DiscussionCommentMapper commentMapper;
    private final DiscussionCommentLikeMapper commentLikeMapper;
    private final DiscussionLikeMapper likeMapper;
    private final DiscussionUserBanMapper banMapper;
    private final UserMapper userMapper;
    private final ContentModerationService contentModerationService;

    @GetMapping("/page")
    public Result<Page<DiscussionPostVO>> page(@RequestParam(defaultValue = "1") Long current,
                                               @RequestParam(defaultValue = "10") Long size,
                                               @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<DiscussionPost> wrapper = new LambdaQueryWrapper<DiscussionPost>()
                .eq(DiscussionPost::getDeleted, 0)
                .like(keyword != null && !keyword.isBlank(), DiscussionPost::getTitle, keyword)
                .orderByDesc(DiscussionPost::getPinned)
                .orderByDesc(DiscussionPost::getFeatured)
                .orderByDesc(DiscussionPost::getCreateTime);
        Page<DiscussionPost> page = postMapper.selectPage(Page.of(current, size), wrapper);
        Page<DiscussionPostVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toPostVO).toList());
        return Result.success(result);
    }

    @PostMapping("/post")
    public Result<Long> createPost(@Valid @RequestBody DiscussionPostCreateDTO dto) {
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
        return Result.success(post.getId());
    }

    @PostMapping("/comment")
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> comment(@Valid @RequestBody DiscussionCommentCreateDTO dto) {
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
        return Result.success(comment.getId());
    }

    @PostMapping("/like/{postId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> like(@PathVariable Long postId) {
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
        return Result.success();
    }

    @PostMapping("/comment/like/{commentId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> likeComment(@PathVariable Long commentId) {
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
        return Result.success();
    }

    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        DiscussionPost post = requirePost(id);
        if (!canDeletePost(post)) {
            throw new BusinessException(403, "无权删除该帖子");
        }
        post.setDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        return Result.success();
    }

    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
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
        return Result.success();
    }

    @PutMapping("/pin")
    public Result<Void> pin(@Valid @RequestBody DiscussionPostFlagDTO dto) {
        DiscussionPost post = requirePost(dto.getPostId());
        requireManagePost(post);
        post.setPinned(normalizeFlag(dto.getValue()));
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        return Result.success();
    }

    @PutMapping("/feature")
    public Result<Void> feature(@Valid @RequestBody DiscussionPostFlagDTO dto) {
        DiscussionPost post = requirePost(dto.getPostId());
        requireManagePost(post);
        post.setFeatured(normalizeFlag(dto.getValue()));
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
        return Result.success();
    }

    @PostMapping("/ban")
    public Result<Void> ban(@Valid @RequestBody DiscussionBanDTO dto) {
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
        return Result.success();
    }

    @PostMapping("/unban/{userId}")
    public Result<Void> unban(@PathVariable Long userId) {
        RoleUtils.requireAny("ADMIN");
        DiscussionUserBan ban = banMapper.selectOne(new LambdaQueryWrapper<DiscussionUserBan>()
                .eq(DiscussionUserBan::getUserId, userId));
        if (ban != null) {
            ban.setStatus(0);
            ban.setUpdateTime(LocalDateTime.now());
            banMapper.updateById(ban);
        }
        return Result.success();
    }

    private DiscussionPostVO toPostVO(DiscussionPost post) {
        Long userId = UserContext.getUserId();
        User author = userMapper.selectById(post.getAuthorId());
        boolean liked = userId != null && likeMapper.selectCount(new LambdaQueryWrapper<DiscussionLike>()
                .eq(DiscussionLike::getPostId, post.getId())
                .eq(DiscussionLike::getUserId, userId)
                .eq(DiscussionLike::getDeleted, 0)) > 0;
        List<DiscussionCommentVO> comments = commentMapper.selectList(new LambdaQueryWrapper<DiscussionComment>()
                        .eq(DiscussionComment::getPostId, post.getId())
                        .eq(DiscussionComment::getDeleted, 0)
                        .orderByAsc(DiscussionComment::getCreateTime))
                .stream()
                .map(comment -> DiscussionCommentVO.from(
                        comment,
                        userMapper.selectById(comment.getUserId()),
                        countCommentLikes(comment.getId()),
                        hasLikedComment(comment.getId(), userId),
                        canDeleteComment(comment)))
                .toList();
        return DiscussionPostVO.from(post, author, liked, canDeletePost(post), canManagePost(post), comments);
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

    private boolean hasLikedComment(Long commentId, Long userId) {
        return userId != null && commentLikeMapper.selectCount(new LambdaQueryWrapper<DiscussionCommentLike>()
                .eq(DiscussionCommentLike::getCommentId, commentId)
                .eq(DiscussionCommentLike::getUserId, userId)
                .eq(DiscussionCommentLike::getDeleted, 0)) > 0;
    }

    private long countCommentLikes(Long commentId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<DiscussionCommentLike>()
                .eq(DiscussionCommentLike::getCommentId, commentId)
                .eq(DiscussionCommentLike::getDeleted, 0));
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
}
