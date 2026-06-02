package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/page")
    public Result<Page<OperationLog>> page(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size) {
        return Result.success(operationLogMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime)));
    }

    @GetMapping("/{id}")
    public Result<OperationLog> detail(@PathVariable Long id) {
        return Result.success(operationLogMapper.selectById(id));
    }
}
