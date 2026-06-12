package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.KnowledgeSaveDTO;
import com.campus.assistant.entity.KnowledgeEntry;
import com.campus.assistant.service.KnowledgeService;
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
 * 知识库控制器，负责知识条目管理和检索请求。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/page")
    public Result<Page<KnowledgeEntry>> page(@RequestParam(defaultValue = "1") Long current,
                                              @RequestParam(defaultValue = "10") Long size,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(knowledgeService.page(current, size, category, keyword));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeEntry> detail(@PathVariable Long id) {
        return Result.success(knowledgeService.detail(id));
    }

    @GetMapping("/manage/page")
    public Result<Page<KnowledgeEntry>> managePage(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam(defaultValue = "10") Long size) {
        return Result.success(knowledgeService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody KnowledgeSaveDTO dto) {
        return Result.success(knowledgeService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody KnowledgeSaveDTO dto) {
        knowledgeService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success();
    }
}
