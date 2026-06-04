package com.campus.assistant.vo;

import com.campus.assistant.entity.NoticeComment;
import com.campus.assistant.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeCommentVO {

    private Long id;
    private Long noticeId;
    private Long userId;
    private String realName;
    private String roleCode;
    private String college;
    private String content;
    private LocalDateTime createTime;

    public static NoticeCommentVO from(NoticeComment comment, User user) {
        return NoticeCommentVO.builder()
                .id(comment.getId())
                .noticeId(comment.getNoticeId())
                .userId(comment.getUserId())
                .realName(user == null ? "未知用户" : user.getRealName())
                .roleCode(user == null ? "" : user.getRoleCode())
                .college(user == null ? "" : user.getCollege())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .build();
    }
}
