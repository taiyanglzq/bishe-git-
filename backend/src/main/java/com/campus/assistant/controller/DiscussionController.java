package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.DiscussionBanDTO;
import com.campus.assistant.dto.DiscussionCommentCreateDTO;
import com.campus.assistant.dto.DiscussionPostCreateDTO;
import com.campus.assistant.dto.DiscussionPostFlagDTO;
import com.campus.assistant.service.DiscussionService;
import com.campus.assistant.vo.DiscussionPostVO;
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

/**
 * 讨论交流控制器，负责接收讨论区相关请求并调用讨论服务完成业务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/discussion")
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/page")
    public Result<Page<DiscussionPostVO>> page(@RequestParam(defaultValue = "1") Long current,
                                               @RequestParam(defaultValue = "10") Long size,
                                               @RequestParam(required = false) String keyword) {
        return Result.success(discussionService.page(current, size, keyword));
    }

    @PostMapping("/post")
    public Result<Long> createPost(@Valid @RequestBody DiscussionPostCreateDTO dto) {
        return Result.success(discussionService.createPost(dto));
    }

    @PostMapping("/comment")
    public Result<Long> comment(@Valid @RequestBody DiscussionCommentCreateDTO dto) {
        return Result.success(discussionService.comment(dto));
    }

    @PostMapping("/like/{postId}")
    public Result<Void> like(@PathVariable Long postId) {
        discussionService.like(postId);
        return Result.success();
    }

    @PostMapping("/comment/like/{commentId}")
    public Result<Void> likeComment(@PathVariable Long commentId) {
        discussionService.likeComment(commentId);
        return Result.success();
    }

    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        discussionService.deletePost(id);
        return Result.success();
    }

    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        discussionService.deleteComment(id);
        return Result.success();
    }

    @PutMapping("/pin")
    public Result<Void> pin(@Valid @RequestBody DiscussionPostFlagDTO dto) {
        discussionService.pin(dto);
        return Result.success();
    }

    @PutMapping("/feature")
    public Result<Void> feature(@Valid @RequestBody DiscussionPostFlagDTO dto) {
        discussionService.feature(dto);
        return Result.success();
    }

    @PostMapping("/ban")
    public Result<Void> ban(@Valid @RequestBody DiscussionBanDTO dto) {
        discussionService.ban(dto);
        return Result.success();
    }

    @PostMapping("/unban/{userId}")
    public Result<Void> unban(@PathVariable Long userId) {
        discussionService.unban(userId);
        return Result.success();
    }
}
