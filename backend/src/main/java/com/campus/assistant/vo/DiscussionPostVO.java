package com.campus.assistant.vo;

import com.campus.assistant.entity.DiscussionPost;
import com.campus.assistant.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiscussionPostVO {

    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String college;
    private String title;
    private String content;
    private String imageUrl;
    private Integer pinned;
    private Integer featured;
    private Long likeCount;
    private Long commentCount;
    private Boolean liked;
    private Boolean canDelete;
    private Boolean canManage;
    private LocalDateTime createTime;
    private List<DiscussionCommentVO> comments;

    public static DiscussionPostVO from(DiscussionPost post, User author, boolean liked, boolean canDelete, boolean canManage, List<DiscussionCommentVO> comments) {
        return DiscussionPostVO.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorName(author == null ? "未知用户" : author.getRealName())
                .authorRole(author == null ? "" : author.getRoleCode())
                .college(post.getCollege())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .pinned(post.getPinned())
                .featured(post.getFeatured())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .liked(liked)
                .canDelete(canDelete)
                .canManage(canManage)
                .createTime(post.getCreateTime())
                .comments(comments)
                .build();
    }
}
