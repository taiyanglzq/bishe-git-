package com.campus.assistant.controller;

import com.campus.assistant.common.result.Result;
import com.campus.assistant.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 个性化推荐控制器，负责接收推荐请求并调用推荐服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personal")
    public Result<Map<String, Object>> personal() {
        return Result.success(recommendationService.personal());
    }
}
