package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.NoticeCommentCreateDTO;
import com.campus.assistant.dto.NoticeSaveDTO;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.entity.NoticeComment;
import com.campus.assistant.mapper.NoticeCommentMapper;
import com.campus.assistant.mapper.NoticeMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.vo.NoticeCommentVO;
import com.campus.assistant.vo.NoticeDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeMapper noticeMapper;
    private final NoticeCommentMapper noticeCommentMapper;
    private final UserMapper userMapper;

    @GetMapping("/page")
    public Result<Page<Notice>> page(@RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1);
        applyNoticeVisibility(wrapper);
        wrapper.orderByDesc(Notice::getCreateTime);
        return Result.success(noticeMapper.selectPage(Page.of(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<NoticeDetailVO> detail(@PathVariable Long id) {
        Notice notice = requireVisibleNotice(id);
        notice.setViewCount((notice.getViewCount() == null ? 0L : notice.getViewCount()) + 1);
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return Result.success(NoticeDetailVO.from(notice, queryComments(id)));
    }

    @GetMapping("/{id}/comments")
    public Result<List<NoticeCommentVO>> comments(@PathVariable Long id) {
        requireVisibleNotice(id);
        return Result.success(queryComments(id));
    }

    @PostMapping("/comment")
    public Result<Long> comment(@Valid @RequestBody NoticeCommentCreateDTO dto) {
        requireVisibleNotice(dto.getNoticeId());
        NoticeComment comment = new NoticeComment();
        comment.setNoticeId(dto.getNoticeId());
        comment.setUserId(UserContext.getUserId());
        comment.setContent(dto.getContent());
        comment.setDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        noticeCommentMapper.insert(comment);
        return Result.success(comment.getId());
    }

    @GetMapping("/manage/page")
    public Result<Page<Notice>> managePage(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            wrapper.eq(Notice::getPublisherId, UserContext.getUserId());
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return Result.success(noticeMapper.selectPage(Page.of(current, size), wrapper));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody NoticeSaveDTO dto) {
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
        return Result.success(notice.getId());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody NoticeSaveDTO dto) {
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
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Notice notice = noticeMapper.selectById(id);
        if (notice != null) {
            requireNoticeOwner(notice);
            notice.setDeleted(1);
            notice.setUpdateTime(LocalDateTime.now());
            noticeMapper.updateById(notice);
        }
        return Result.success();
    }

    private void applyNoticeVisibility(LambdaQueryWrapper<Notice> wrapper) {
        if (RoleUtils.hasAny("ADMIN")) {
            return;
        }
        String college = UserContext.get() == null ? null : UserContext.get().getCollege();
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
            String college = UserContext.get() == null ? null : UserContext.get().getCollege();
            boolean schoolVisible = "SCHOOL".equals(notice.getScopeType());
            boolean collegeVisible = college != null && college.equals(notice.getScopeCollege());
            if (!schoolVisible && !collegeVisible) {
                throw new BusinessException(403, "无权查看该公告");
            }
        }
        return notice;
    }

    private List<NoticeCommentVO> queryComments(Long noticeId) {
        return noticeCommentMapper.selectList(new LambdaQueryWrapper<NoticeComment>()
                        .eq(NoticeComment::getNoticeId, noticeId)
                        .eq(NoticeComment::getDeleted, 0)
                        .orderByAsc(NoticeComment::getCreateTime))
                .stream()
                .map(comment -> NoticeCommentVO.from(comment, userMapper.selectById(comment.getUserId())))
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
        String college = scopeCollege == null || scopeCollege.isBlank() ? UserContext.get().getCollege() : scopeCollege;
        if (!RoleUtils.hasAny("ADMIN") && !college.equals(UserContext.get().getCollege())) {
            throw new BusinessException(403, "教师只能发布本院系公告");
        }
        return college;
    }
}
