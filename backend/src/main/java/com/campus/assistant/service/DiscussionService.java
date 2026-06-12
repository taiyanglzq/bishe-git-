package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.DiscussionBanDTO;
import com.campus.assistant.dto.DiscussionCommentCreateDTO;
import com.campus.assistant.dto.DiscussionPostCreateDTO;
import com.campus.assistant.dto.DiscussionPostFlagDTO;
import com.campus.assistant.vo.DiscussionPostVO;

/**
 * 讨论交流服务接口，定义帖子、评论、点赞与讨论区管理相关业务能力。
 */
public interface DiscussionService {

    Page<DiscussionPostVO> page(Long current, Long size, String keyword);

    Long createPost(DiscussionPostCreateDTO dto);

    Long comment(DiscussionCommentCreateDTO dto);

    void like(Long postId);

    void likeComment(Long commentId);

    void deletePost(Long id);

    void deleteComment(Long id);

    void pin(DiscussionPostFlagDTO dto);

    void feature(DiscussionPostFlagDTO dto);

    void ban(DiscussionBanDTO dto);

    void unban(Long userId);
}
