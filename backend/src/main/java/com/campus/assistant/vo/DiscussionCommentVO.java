package com.campus.assistant.vo;

import com.campus.assistant.entity.DiscussionComment;
import com.campus.assistant.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ???? VO???????????????????
 */
@Data
@Builder
public class DiscussionCommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String realName;
    private String roleCode;
    private String college;
    private String content;
    private Long likeCount;
    private Boolean liked;
    private LocalDateTime createTime;
    private Boolean canDelete;

    public static DiscussionCommentVO from(DiscussionComment comment, User user, long likeCount, boolean liked, boolean canDelete) {
        return DiscussionCommentVO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .realName(user == null ? "未知用户" : user.getRealName())
                .roleCode(user == null ? "" : user.getRoleCode())
                .college(user == null ? "" : user.getCollege())
                .content(comment.getContent())
                .likeCount(likeCount)
                .liked(liked)
                .createTime(comment.getCreateTime())
                .canDelete(canDelete)
                .build();
    }
}
