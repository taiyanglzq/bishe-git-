package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.entity.User;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.NoticeMapper;
import com.campus.assistant.mapper.VenueMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ????? ?????????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/recommendation")
public class RecommendationController {

    private final NoticeMapper noticeMapper;
    private final ActivityMapper activityMapper;
    private final VenueMapper venueMapper;

    @GetMapping("/personal")
    public Result<Map<String, Object>> personal() {
        User user = UserContext.get();
        Map<String, Object> result = new HashMap<>();
        result.put("roleCode", user == null ? "GUEST" : user.getRoleCode());
        result.put("notices", noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getDeleted, 0)
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getViewCount)
                .last("limit 5")));
        result.put("activities", activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0)
                .eq(Activity::getStatus, 1)
                .orderByDesc(Activity::getCreateTime)
                .last("limit 5")));
        result.put("venues", venueMapper.selectList(new LambdaQueryWrapper<Venue>()
                .eq(Venue::getDeleted, 0)
                .eq(Venue::getStatus, 1)
                .last("limit 5")));
        result.put("reason", List.of("基于角色推荐", "基于公告热度推荐", "基于近期活动推荐"));
        return Result.success(result);
    }
}
