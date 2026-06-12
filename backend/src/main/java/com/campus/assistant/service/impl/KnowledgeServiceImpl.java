package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.dto.KnowledgeSaveDTO;
import com.campus.assistant.entity.KnowledgeEntry;
import com.campus.assistant.mapper.KnowledgeEntryMapper;
import com.campus.assistant.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 知识库服务实现，负责知识条目管理、Redis 缓存和关键词 RAG 检索。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final String KNOWLEDGE_CACHE_PREFIX = "ca:knowledge:all";
    private static final long KNOWLEDGE_CACHE_TTL_MINUTES = 30L;

    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final CacheClient cacheClient;

    @Override
    public Page<KnowledgeEntry> page(Long current, Long size, String category, String keyword) {
        LambdaQueryWrapper<KnowledgeEntry> wrapper = new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getDeleted, 0)
                .eq(KnowledgeEntry::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(KnowledgeEntry::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(KnowledgeEntry::getQuestion, keyword)
                    .or()
                    .like(KnowledgeEntry::getAnswer, keyword)
                    .or()
                    .like(KnowledgeEntry::getKeywords, keyword));
        }
        wrapper.orderByAsc(KnowledgeEntry::getCategory, KnowledgeEntry::getId);
        return knowledgeEntryMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public KnowledgeEntry detail(Long id) {
        KnowledgeEntry entry = knowledgeEntryMapper.selectById(id);
        if (entry == null || entry.getDeleted() == 1) {
            throw new BusinessException(404, "知识条目不存在");
        }
        return entry;
    }

    @Override
    public String retrieveContext(String question, int maxResults) {
        if (question == null || question.isBlank()) {
            return "";
        }
        // 先从缓存获取全量知识库
        String cacheKey = KNOWLEDGE_CACHE_PREFIX;
        List<KnowledgeEntry> allEntries = cacheClient.getList(cacheKey, KnowledgeEntry.class);
        if (allEntries == null) {
            allEntries = knowledgeEntryMapper.selectList(new LambdaQueryWrapper<KnowledgeEntry>()
                    .eq(KnowledgeEntry::getDeleted, 0)
                    .eq(KnowledgeEntry::getStatus, 1));
            cacheClient.set(cacheKey, allEntries, KNOWLEDGE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        if (allEntries == null || allEntries.isEmpty()) {
            return "";
        }

        // 关键词匹配打分
        String lowered = question.toLowerCase();
        List<ScoredEntry> scored = new ArrayList<>();
        for (KnowledgeEntry entry : allEntries) {
            int score = 0;
            String q = entry.getQuestion() == null ? "" : entry.getQuestion().toLowerCase();
            String a = entry.getAnswer() == null ? "" : entry.getAnswer().toLowerCase();
            String kw = entry.getKeywords() == null ? "" : entry.getKeywords().toLowerCase();

            // 精确匹配加分
            if (lowered.contains(q) || q.contains(lowered)) score += 10;
            // 关键词匹配
            for (String k : kw.split("[,，]")) {
                String trimmed = k.trim();
                if (!trimmed.isEmpty() && lowered.contains(trimmed)) {
                    score += 5;
                }
            }
            // 问题中的词匹配
            for (String word : q.split("\\s+")) {
                if (!word.isEmpty() && lowered.contains(word)) score += 2;
            }
            // 答案中的词匹配
            for (String word : a.split("\\s+")) {
                if (word.length() > 1 && lowered.contains(word)) score += 1;
            }
            if (score > 0) {
                scored.add(new ScoredEntry(entry, score));
            }
        }

        // 按分数降序排列，取前 maxResults 条
        scored.sort((a, b) -> Integer.compare(b.score, a.score));
        List<KnowledgeEntry> topEntries = scored.stream()
                .limit(maxResults)
                .map(se -> se.entry)
                .toList();

        if (topEntries.isEmpty()) {
            return "";
        }

        // 拼接为上下文
        StringBuilder sb = new StringBuilder();
        sb.append("以下是校园助手知识库中与用户问题相关的参考信息：\n\n");
        for (int i = 0; i < topEntries.size(); i++) {
            KnowledgeEntry e = topEntries.get(i);
            sb.append("【参考").append(i + 1).append("】");
            if (StringUtils.hasText(e.getQuestion())) {
                sb.append("问题：").append(e.getQuestion()).append("\n");
            }
            sb.append("答案：").append(e.getAnswer()).append("\n\n");
        }
        sb.append("请根据以上参考信息回答用户问题。如果参考信息不足以回答，请如实说明并给出校园助手其他功能的建议。");
        return sb.toString();
    }

    @Override
    public Page<KnowledgeEntry> managePage(Long current, Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        return knowledgeEntryMapper.selectPage(Page.of(current, size),
                new LambdaQueryWrapper<KnowledgeEntry>()
                        .eq(KnowledgeEntry::getDeleted, 0)
                        .orderByDesc(KnowledgeEntry::getCreateTime));
    }

    @Override
    public Long save(KnowledgeSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setQuestion(dto.getQuestion());
        entry.setAnswer(dto.getAnswer());
        entry.setCategory(dto.getCategory());
        entry.setKeywords(dto.getKeywords());
        entry.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entry.setDeleted(0);
        entry.setCreateTime(LocalDateTime.now());
        entry.setUpdateTime(LocalDateTime.now());
        knowledgeEntryMapper.insert(entry);
        evictKnowledgeCache();
        return entry.getId();
    }

    @Override
    public void update(KnowledgeSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        KnowledgeEntry entry = knowledgeEntryMapper.selectById(dto.getId());
        if (entry == null || entry.getDeleted() == 1) {
            throw new BusinessException(404, "知识条目不存在");
        }
        entry.setQuestion(dto.getQuestion());
        entry.setAnswer(dto.getAnswer());
        entry.setCategory(dto.getCategory());
        entry.setKeywords(dto.getKeywords());
        entry.setStatus(dto.getStatus() == null ? entry.getStatus() : dto.getStatus());
        entry.setUpdateTime(LocalDateTime.now());
        knowledgeEntryMapper.updateById(entry);
        evictKnowledgeCache();
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        KnowledgeEntry entry = knowledgeEntryMapper.selectById(id);
        if (entry != null) {
            knowledgeEntryMapper.update(null, new LambdaUpdateWrapper<KnowledgeEntry>()
                    .eq(KnowledgeEntry::getId, id)
                    .set(KnowledgeEntry::getDeleted, 1)
                    .set(KnowledgeEntry::getUpdateTime, LocalDateTime.now()));
            evictKnowledgeCache();
        }
    }

    private void evictKnowledgeCache() {
        cacheClient.delete(KNOWLEDGE_CACHE_PREFIX);
    }

    /**
     * 内部评分条目
     */
    private record ScoredEntry(KnowledgeEntry entry, int score) {}
}
