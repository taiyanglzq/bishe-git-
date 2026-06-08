package com.campus.assistant.vo;

import com.campus.assistant.entity.Notice;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ?? VO?????????????????
 */
@Data
@Builder
public class NoticeDetailVO {

    private Long id;
    private String title;
    private String category;
    private String content;
    private Long viewCount;
    private String scopeType;
    private String scopeCollege;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<NoticeCommentVO> comments;

    public static NoticeDetailVO from(Notice notice, List<NoticeCommentVO> comments) {
        return NoticeDetailVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .category(notice.getCategory())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .scopeType(notice.getScopeType())
                .scopeCollege(notice.getScopeCollege())
                .createTime(notice.getCreateTime())
                .updateTime(notice.getUpdateTime())
                .comments(comments)
                .build();
    }
}
