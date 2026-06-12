package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库条目实体，对应 ca_knowledge 表
 */
@Data
@TableName("ca_knowledge")
public class KnowledgeEntry {

    /**
     * 知识条目ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 问题/标题
     */
    private String question;

    /**
     * 答案/内容
     */
    private String answer;

    /**
     * 分类：校园规章、学习指导、生活服务、常见问题等
     */
    private String category;

    /**
     * 关键词，用逗号分隔，用于检索匹配
     */
    private String keywords;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
