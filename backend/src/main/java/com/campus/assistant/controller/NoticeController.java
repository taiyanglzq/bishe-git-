package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.NoticeCommentCreateDTO;
import com.campus.assistant.dto.NoticeSaveDTO;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.service.NoticeService;
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

import java.util.List;

/**
 * 公告控制器，负责接收公告查询、评论和后台管理请求并调用公告服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/page")
    public Result<Page<Notice>> page(@RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size) {
        return Result.success(noticeService.page(current, size));
    }

    @GetMapping("/{id}")
    public Result<NoticeDetailVO> detail(@PathVariable Long id) {
        return Result.success(noticeService.detail(id));
    }

    @GetMapping("/{id}/comments")
    public Result<List<NoticeCommentVO>> comments(@PathVariable Long id) {
        return Result.success(noticeService.comments(id));
    }

    @PostMapping("/comment")
    public Result<Long> comment(@Valid @RequestBody NoticeCommentCreateDTO dto) {
        return Result.success(noticeService.comment(dto));
    }

    @GetMapping("/manage/page")
    public Result<Page<Notice>> managePage(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size) {
        return Result.success(noticeService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody NoticeSaveDTO dto) {
        return Result.success(noticeService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody NoticeSaveDTO dto) {
        noticeService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }

    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        noticeService.approve(id);
        return Result.success();
    }

    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id) {
        noticeService.reject(id);
        return Result.success();
    }
}
