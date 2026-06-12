package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.KnowledgeSaveDTO;
import com.campus.assistant.entity.KnowledgeEntry;

/**
 * 知识库服务接口，定义知识条目管理和 RAG 检索能力。
 */
public interface KnowledgeService {

    /**
     * 知识库分页查询
     */
    Page<KnowledgeEntry> page(Long current, Long size, String category, String keyword);

    /**
     * 知识条目详情
     */
    KnowledgeEntry detail(Long id);

    /**
     * RAG 检索：根据用户问题从知识库中检索相关条目，返回拼接后的上下文
     */
    String retrieveContext(String question, int maxResults);

    /**
     * 管理端分页
     */
    Page<KnowledgeEntry> managePage(Long current, Long size);

    /**
     * 新增知识条目
     */
    Long save(KnowledgeSaveDTO dto);

    /**
     * 编辑知识条目
     */
    void update(KnowledgeSaveDTO dto);

    /**
     * 删除知识条目
     */
    void delete(Long id);
}
