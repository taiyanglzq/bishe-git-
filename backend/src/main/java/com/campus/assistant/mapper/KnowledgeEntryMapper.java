package com.campus.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.assistant.entity.KnowledgeEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库 Mapper，负责 ca_knowledge 表的数据库操作
 */
@Mapper
public interface KnowledgeEntryMapper extends BaseMapper<KnowledgeEntry> {
}
