package com.campus.assistant.service;

import java.util.Map;

/**
 * 个性化推荐服务接口，定义推荐结果聚合与返回相关业务能力。
 */
public interface RecommendationService {

    Map<String, Object> personal();
}
