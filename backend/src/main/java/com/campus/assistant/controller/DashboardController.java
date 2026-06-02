package com.campus.assistant.controller;

import com.campus.assistant.common.result.Result;
import com.campus.assistant.service.DashboardService;
import com.campus.assistant.vo.DashboardStatsVO;
import com.campus.assistant.vo.DashboardVO;
import com.campus.assistant.vo.DashboardWorkbenchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardVO> summary() {
        return Result.success(dashboardService.summary());
    }

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.stats());
    }

    @GetMapping("/workbench")
    public Result<DashboardWorkbenchVO> workbench() {
        return Result.success(dashboardService.workbench());
    }
}
