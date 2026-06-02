package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.NoticeSaveDTO;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.mapper.NoticeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeMapper noticeMapper;

    @GetMapping("/page")
    public Result<Page<Notice>> page(@RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0);
        if (!RoleUtils.hasAny("ADMIN")) {
            String college = UserContext.get() == null ? null : UserContext.get().getCollege();
            wrapper.and(query -> query.eq(Notice::getScopeType, "SCHOOL")
                    .or()
                    .eq(Notice::getScopeCollege, college));
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        return Result.success(noticeMapper.selectPage(Page.of(current, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.success(noticeMapper.selectById(id));
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
        if (notice == null) {
            return Result.fail(404, "公告不存在");
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
