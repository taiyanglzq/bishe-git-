package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志控制器，负责接收日志查询请求并调用日志服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {

    private final LogService logService;

    @GetMapping("/page")
    public Result<Page<OperationLog>> page(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size) {
        return Result.success(logService.page(current, size));
    }

    @GetMapping("/{id}")
    public Result<OperationLog> detail(@PathVariable Long id) {
        return Result.success(logService.detail(id));
    }
}
