package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.ActivityEnroll;
import com.campus.assistant.entity.Booking;
import com.campus.assistant.entity.Checkin;
import com.campus.assistant.entity.Notification;
import com.campus.assistant.entity.User;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.mapper.ActivityEnrollMapper;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.BookingMapper;
import com.campus.assistant.mapper.CheckinMapper;
import com.campus.assistant.mapper.NotificationMapper;
import com.campus.assistant.mapper.NoticeMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.service.DashboardService;
import com.campus.assistant.vo.DashboardStatsVO;
import com.campus.assistant.vo.DashboardVO;
import com.campus.assistant.vo.DashboardWorkbenchVO;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final NoticeMapper noticeMapper;
    private final VenueMapper venueMapper;
    private final ActivityMapper activityMapper;
    private final BookingMapper bookingMapper;
    private final CheckinMapper checkinMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final NotificationMapper notificationMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

    @Override
    public DashboardVO summary() {
        return DashboardVO.builder()
                .userCount(userMapper.selectCount(null))
                .noticeCount(noticeMapper.selectCount(null))
                .venueCount(venueMapper.selectCount(null))
                .activityCount(activityMapper.selectCount(null))
                .bookingCount(bookingMapper.selectCount(null))
                .checkinCount(checkinMapper.selectCount(null))
                .build();
    }

    @Override
    public DashboardStatsVO stats() {
        Long enrollCount = activityEnrollMapper.selectCount(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getDeleted, 0));
        Long checkinCount = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getDeleted, 0));
        double checkinRate = enrollCount == 0 ? 0 : Math.round(checkinCount * 10000.0 / enrollCount) / 100.0;

        return DashboardStatsVO.builder()
                .summary(summary())
                .bookingStatus(List.of(
                        item("待审核", countBookingStatus("PENDING")),
                        item("已通过", countBookingStatus("APPROVED")),
                        item("已驳回", countBookingStatus("REJECTED")),
                        item("已取消", countBookingStatus("CANCELLED"))
                ))
                .activityEnrollRank(activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                                .eq(Activity::getDeleted, 0)
                                .orderByDesc(Activity::getEnrolledCount)
                                .last("limit 5"))
                        .stream()
                        .map(activity -> item(activity.getTitle(), activity.getEnrolledCount() == null ? 0L : activity.getEnrolledCount().longValue()))
                        .toList())
                .venueBookingRank(venueMapper.selectList(new LambdaQueryWrapper<Venue>()
                                .eq(Venue::getDeleted, 0))
                        .stream()
                        .map(venue -> item(venue.getName(), bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                                .eq(Booking::getVenueId, venue.getId())
                                .eq(Booking::getDeleted, 0))))
                        .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
                        .limit(5)
                        .toList())
                .enrollCount(enrollCount)
                .checkinCount(checkinCount)
                .checkinRate(checkinRate)
                .build();
    }

    @Override
    public DashboardWorkbenchVO workbench() {
        User currentUser = UserContext.get();
        Long userId = UserContext.getUserId();
        String roleCode = currentUser == null ? "STUDENT" : currentUser.getRoleCode();
        Long pendingBookingCount = countVisiblePendingBookings(currentUser);
        Long unreadNotificationCount = userId == null ? 0L : notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .eq(Notification::getReadStatus, 0)
                .eq(Notification::getDeleted, 0));

        return DashboardWorkbenchVO.builder()
                .roleName(roleName(roleCode))
                .welcomeText(welcomeText(currentUser, roleCode))
                .pendingBookingCount(pendingBookingCount)
                .unreadNotificationCount(unreadNotificationCount)
                .todayBookingCount(countTodayBookings(currentUser))
                .upcomingActivityCount(countUpcomingActivities(currentUser))
                .todos(buildTodos(currentUser))
                .schedules(buildSchedules(currentUser))
                .build();
    }

    private Long countBookingStatus(String status) {
        return bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, status)
                .eq(Booking::getDeleted, 0));
    }

    private DashboardStatsVO.NameValueVO item(String name, Long value) {
        return DashboardStatsVO.NameValueVO.builder()
                .name(name)
                .value(value)
                .build();
    }

    private Long countVisiblePendingBookings(User currentUser) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStatus, "PENDING")
                .eq(Booking::getDeleted, 0);
        applyBookingVisibility(wrapper, currentUser);
        return bookingMapper.selectCount(wrapper);
    }

    private Long countTodayBookings(User currentUser) {
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getBookingDate, LocalDate.now())
                .eq(Booking::getDeleted, 0);
        applyBookingVisibility(wrapper, currentUser);
        return bookingMapper.selectCount(wrapper);
    }

    private Long countUpcomingActivities(User currentUser) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0)
                .eq(Activity::getStatus, 1)
                .ge(Activity::getStartTime, LocalDateTime.now());
        applyActivityVisibility(wrapper, currentUser);
        return activityMapper.selectCount(wrapper);
    }

    private List<DashboardWorkbenchVO.WorkbenchItemVO> buildTodos(User currentUser) {
        List<DashboardWorkbenchVO.WorkbenchItemVO> todos = new ArrayList<>();
        if (RoleUtils.hasAny("TEACHER", "ADMIN")) {
            LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getStatus, "PENDING")
                    .eq(Booking::getDeleted, 0)
                    .orderByAsc(Booking::getCreateTime)
                    .last("limit 5");
            applyBookingVisibility(bookingWrapper, currentUser);
            bookingMapper.selectList(bookingWrapper).forEach(booking -> todos.add(workbenchItem(
                    "待审核场地预约",
                    bookingUserName(booking.getStudentId()) + " 申请 " + venueName(booking.getVenueId()) + " " + booking.getTimeRange(),
                    "预约审核",
                    booking.getCreateTime(),
                    booking.getId()
            )));
            return todos;
        }

        LambdaQueryWrapper<Booking> myPendingWrapper = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStudentId, UserContext.getUserId())
                .eq(Booking::getStatus, "PENDING")
                .eq(Booking::getDeleted, 0)
                .orderByDesc(Booking::getCreateTime)
                .last("limit 3");
        bookingMapper.selectList(myPendingWrapper).forEach(booking -> todos.add(workbenchItem(
                "预约等待审核",
                venueName(booking.getVenueId()) + " " + booking.getBookingDate() + " " + booking.getTimeRange(),
                "我的预约",
                booking.getCreateTime(),
                booking.getId()
        )));

        Long userId = UserContext.getUserId();
        if (userId != null) {
            notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getReceiverId, userId)
                    .eq(Notification::getReadStatus, 0)
                    .eq(Notification::getDeleted, 0)
                    .orderByDesc(Notification::getCreateTime)
                    .last("limit 3"))
                    .forEach(notification -> todos.add(workbenchItem(
                            notification.getTitle(),
                            notification.getContent(),
                            "未读通知",
                            notification.getCreateTime(),
                            notification.getId()
                    )));
        }
        return todos.stream().limit(5).toList();
    }

    private List<DashboardWorkbenchVO.WorkbenchItemVO> buildSchedules(User currentUser) {
        List<DashboardWorkbenchVO.WorkbenchItemVO> schedules = new ArrayList<>();
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0)
                .eq(Activity::getStatus, 1)
                .ge(Activity::getStartTime, LocalDateTime.now())
                .orderByAsc(Activity::getStartTime)
                .last("limit 5");
        applyActivityVisibility(activityWrapper, currentUser);
        activityMapper.selectList(activityWrapper).forEach(activity -> schedules.add(workbenchItem(
                activity.getTitle(),
                activity.getLocation() + "，已报名 " + safeInt(activity.getEnrolledCount()) + "/" + safeInt(activity.getCapacity()),
                "近期活动",
                activity.getStartTime(),
                activity.getId()
        )));
        return schedules;
    }

    private void applyBookingVisibility(LambdaQueryWrapper<Booking> wrapper, User currentUser) {
        if (RoleUtils.hasAny("ADMIN")) {
            return;
        }
        if (RoleUtils.hasAny("TEACHER")) {
            if (currentUser == null || currentUser.getCollege() == null) {
                wrapper.eq(Booking::getStudentId, -1L);
                return;
            }
            List<Long> studentIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                            .eq(User::getRoleCode, "STUDENT")
                            .eq(User::getCollege, currentUser.getCollege())
                            .eq(User::getDeleted, 0))
                    .stream()
                    .map(User::getId)
                    .toList();
            if (studentIds.isEmpty()) {
                wrapper.eq(Booking::getStudentId, -1L);
            } else {
                wrapper.in(Booking::getStudentId, studentIds);
            }
            return;
        }
        wrapper.eq(Booking::getStudentId, UserContext.getUserId());
    }

    private void applyActivityVisibility(LambdaQueryWrapper<Activity> wrapper, User currentUser) {
        if (RoleUtils.hasAny("ADMIN")) {
            return;
        }
        String college = currentUser == null ? null : currentUser.getCollege();
        wrapper.and(query -> query.eq(Activity::getScopeType, "SCHOOL")
                .or()
                .eq(Activity::getScopeCollege, college));
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
            return name + "，请关注全校预约审核、资源维护和系统运行数据。";
        }
        if ("TEACHER".equals(roleCode)) {
            return name + "，请及时处理本院系学生预约，并维护自己发布的公告和活动。";
        }
        return name + "，这里汇总你的预约、活动、签到和未读通知。";
    }

    private String bookingUserName(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? "未知学生" : user.getRealName();
    }

    private String venueName(Long venueId) {
        Venue venue = venueMapper.selectById(venueId);
        return venue == null ? "未知场地" : venue.getName();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
