package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.entity.Course;
import com.campus.assistant.entity.Exam;
import com.campus.assistant.entity.Notification;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.CourseMapper;
import com.campus.assistant.mapper.ExamMapper;
import com.campus.assistant.mapper.NotificationMapper;
import com.campus.assistant.mapper.NoticeMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.DashboardService;
import com.campus.assistant.vo.DashboardStatsVO;
import com.campus.assistant.vo.DashboardVO;
import com.campus.assistant.vo.DashboardWorkbenchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据看板服务实现，负责组装首页统计数据和工作台内容，并对高频读取结果做缓存。
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
    private static final long STATS_TTL_MINUTES = 5L;
    private static final long WORKBENCH_TTL_MINUTES = 3L;

    private final UserMapper userMapper;
    private final NoticeMapper noticeMapper;
    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final NotificationMapper notificationMapper;
    private final CacheClient cacheClient;

    @Override
    public DashboardVO summary() {
        return stats().getSummary();
    }

    @Override
    public DashboardStatsVO stats() {
        String roleCode = currentRoleCode();
        String cacheKey = CacheKeyConstants.DASHBOARD_STATS + roleCode;
        DashboardStatsVO cached = cacheClient.get(cacheKey, DashboardStatsVO.class);
        if (cached != null) {
            return cached;
        }

        DashboardVO summary = buildSummary();

        DashboardStatsVO result = DashboardStatsVO.builder()
                .summary(summary)
                .build();
        cacheClient.set(cacheKey, result, STATS_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public DashboardWorkbenchVO workbench() {
        User currentUser = UserContext.get();
        Long userId = UserContext.getUserId();
        String roleCode = currentRoleCode();
        String cacheKey = CacheKeyConstants.DASHBOARD_WORKBENCH + roleCode + ":" + (userId == null ? 0L : userId);
        DashboardWorkbenchVO cached = cacheClient.get(cacheKey, DashboardWorkbenchVO.class);
        if (cached != null) {
            return cached;
        }

        Long unreadNotificationCount = userId == null ? 0L : notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0));

        DashboardWorkbenchVO result = DashboardWorkbenchVO.builder()
                .roleName(roleName(roleCode))
                .welcomeText(welcomeText(currentUser, roleCode))
                .unreadNotificationCount(unreadNotificationCount)
                .todos(buildTodos(currentUser))
                .schedules(buildSchedules(currentUser))
                .build();
        cacheClient.set(cacheKey, result, WORKBENCH_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    private DashboardVO buildSummary() {
        return DashboardVO.builder()
                .userCount(userMapper.selectCount(null))
                .noticeCount(noticeMapper.selectCount(null))
                .courseCount(courseMapper.selectCount(null))
                .examCount(examMapper.selectCount(null))
                .build();
    }

    private DashboardStatsVO.NameValueVO item(String name, Long value) {
        return DashboardStatsVO.NameValueVO.builder()
                .name(name)
                .value(value)
                .build();
    }

    private List<DashboardWorkbenchVO.WorkbenchItemVO> buildTodos(User currentUser) {
        List<DashboardWorkbenchVO.WorkbenchItemVO> todos = new ArrayList<>();

        Long userId = UserContext.getUserId();
        if (userId != null) {
            notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReceiverId, userId)
                    .eq(Notification::getReadStatus, 0)
                    .eq(Notification::getDeleted, 0)
                    .orderByDesc(Notification::getCreateTime)
                    .last("limit 20")).forEach(notification -> todos.add(workbenchItem(
                    notification.getTitle(),
                    notification.getContent(),
                    "未读通知",
                    notification.getCreateTime(),
                    notification.getId()
            )));
        }
        return todos;
    }

    private List<DashboardWorkbenchVO.WorkbenchItemVO> buildSchedules(User currentUser) {
        List<DashboardWorkbenchVO.WorkbenchItemVO> schedules = new ArrayList<>();

        // 近期考试
        examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getDeleted, 0)
                .eq(Exam::getStatus, 1)
                .ge(Exam::getExamDate, java.time.LocalDate.now())
                .orderByAsc(Exam::getExamDate)
                .last("limit 5")).forEach(exam -> schedules.add(workbenchItem(
                exam.getCourseName(),
                exam.getExamDate() + " " + exam.getStartTime() + "-" + exam.getEndTime() + " · " + exam.getLocation(),
                "近期考试",
                exam.getCreateTime(),
                exam.getId()
        )));

        return schedules;
    }

    private DashboardWorkbenchVO.WorkbenchItemVO workbenchItem(String title, String description, String type, LocalDateTime time, Long bizId) {
        return DashboardWorkbenchVO.WorkbenchItemVO.builder()
                .title(title)
                .description(description)
                .type(type)
                .timeText(time == null ? "" : DATE_TIME_FORMATTER.format(time))
                .bizId(bizId)
                .build();
    }

    private String currentRoleCode() {
        User currentUser = UserContext.get();
        return currentUser == null || currentUser.getRoleCode() == null ? "STUDENT" : currentUser.getRoleCode();
    }

    private String roleName(String roleCode) {
        return switch (roleCode) {
            case "ADMIN" -> "管理员";
            case "TEACHER" -> "教师";
            default -> "学生";
        };
    }

    private String welcomeText(User user, String roleCode) {
        String name = user == null ? "同学" : user.getRealName();
        if ("ADMIN".equals(roleCode)) {
            return name + "，请关注系统运行数据、用户管理和公告维护。";
        }
        if ("TEACHER".equals(roleCode)) {
            return name + "，请及时维护课程和考试信息，关注本院系学生动态。";
        }
        return name + "，这里汇总你的课程、考试、图书借阅和未读通知。";
    }
}
