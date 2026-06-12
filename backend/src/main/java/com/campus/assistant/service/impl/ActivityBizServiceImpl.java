package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.cache.CacheClient;
import com.campus.assistant.common.cache.CacheKeyConstants;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.ActivityEnrollDTO;
import com.campus.assistant.dto.ActivitySaveDTO;
import com.campus.assistant.dto.CheckinDTO;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.ActivityEnroll;
import com.campus.assistant.entity.Checkin;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.mapper.ActivityEnrollMapper;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.CheckinMapper;
import com.campus.assistant.mapper.OperationLogMapper;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.service.ActivityBizService;
import com.campus.assistant.service.CacheEvictService;
import com.campus.assistant.service.NotificationService;
import com.campus.assistant.vo.ActivityRecordVO;
import com.campus.assistant.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 活动报名与签到服务实现，负责活动列表展示、报名、取消报名和签到等核心业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class ActivityBizServiceImpl implements ActivityBizService {

    private static final long ACTIVITY_PAGE_TTL_MINUTES = 10L;

    private final ActivityMapper activityMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final CheckinMapper checkinMapper;
    private final OperationLogMapper operationLogMapper;
    private final VenueMapper venueMapper;
    private final NotificationService notificationService;
    private final CacheClient cacheClient;
    private final CacheEvictService cacheEvictService;

    @Override
    public Page<ActivityVO> page(Long current, Long size) {
        Long userId = UserContext.getUserId();
        String roleCode = currentRoleCode();
        String college = currentCollege();
        String cacheKey = CacheKeyConstants.ACTIVITY_PAGE + current + ":" + size + ":" + roleCode + ":" + safeCollegeSegment(college) + ":" + safeUserSegment(userId);
        Page<ActivityVO> cached = cacheClient.get(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0);
        if (!RoleUtils.hasAny("ADMIN")) {
            wrapper.and(query -> query.eq(Activity::getScopeType, "SCHOOL")
                    .or()
                    .eq(Activity::getScopeCollege, college));
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> page = activityMapper.selectPage(Page.of(current, size), wrapper);

        Set<Long> activityIds = page.getRecords().stream().map(Activity::getId).collect(Collectors.toSet());
        Set<Long> enrolledIds = Collections.emptySet();
        Set<Long> checkedInIds = Collections.emptySet();
        if (userId != null && !activityIds.isEmpty()) {
            enrolledIds = activityEnrollMapper.selectList(new LambdaQueryWrapper<ActivityEnroll>()
                            .select(ActivityEnroll::getActivityId)
                            .eq(ActivityEnroll::getStudentId, userId)
                            .eq(ActivityEnroll::getStatus, "ENROLLED")
                            .eq(ActivityEnroll::getDeleted, 0)
                            .in(ActivityEnroll::getActivityId, activityIds))
                    .stream()
                    .map(ActivityEnroll::getActivityId)
                    .collect(Collectors.toSet());
            checkedInIds = checkinMapper.selectList(new LambdaQueryWrapper<Checkin>()
                            .select(Checkin::getActivityId)
                            .eq(Checkin::getStudentId, userId)
                            .eq(Checkin::getDeleted, 0)
                            .in(Checkin::getActivityId, activityIds))
                    .stream()
                    .map(Checkin::getActivityId)
                    .collect(Collectors.toSet());
        }

        Page<ActivityVO> result = Page.of(current, size, page.getTotal());
        Set<Long> finalEnrolledIds = enrolledIds;
        Set<Long> finalCheckedInIds = checkedInIds;
        result.setRecords(page.getRecords().stream()
                .map(activity -> ActivityVO.from(
                        activity,
                        finalEnrolledIds.contains(activity.getId()),
                        finalCheckedInIds.contains(activity.getId())))
                .toList());
        cacheClient.set(cacheKey, result, ACTIVITY_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public Page<Activity> managePage(Long current, Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            wrapper.eq(Activity::getPublisherId, UserContext.getUserId());
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        return activityMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Long save(ActivitySaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Venue venue = requireVenue(dto.getVenueId());
        validateActivityTime(dto);
        validateCapacity(dto, venue);
        Activity activity = new Activity();
        fillActivity(activity, dto, venue);
        activity.setEnrolledCount(0);
        activity.setPublisherId(UserContext.getUserId());
        activity.setDeleted(0);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        evictActivityCaches();
        return activity.getId();
    }

    @Override
    public void update(ActivitySaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Activity activity = activityMapper.selectById(dto.getId());
        if (activity == null || activity.getDeleted() == 1) {
            throw new BusinessException(404, "活动不存在");
        }
        requireActivityOwner(activity);
        Venue venue = requireVenue(dto.getVenueId());
        validateActivityTime(dto);
        validateCapacity(dto, venue);
        fillActivity(activity, dto, venue);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        evictActivityCaches();
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Activity activity = activityMapper.selectById(id);
        if (activity != null) {
            requireActivityOwner(activity);
            activity.setDeleted(1);
            activity.setUpdateTime(LocalDateTime.now());
            activityMapper.updateById(activity);
            evictActivityCaches();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enroll(ActivityEnrollDTO dto) {
        Long userId = requireLogin();
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null || activity.getStatus() == 0) {
            throw new BusinessException(404, "活动不存在或不可报名");
        }
        Long duplicated = activityEnrollMapper.selectCount(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getActivityId, dto.getActivityId())
                .eq(ActivityEnroll::getStudentId, userId)
                .eq(ActivityEnroll::getStatus, "ENROLLED")
                .eq(ActivityEnroll::getDeleted, 0));
        if (duplicated > 0) {
            throw new BusinessException(409, "同一学生不能重复报名同一活动");
        }
        Integer enrolledCount = activity.getEnrolledCount() == null ? 0 : activity.getEnrolledCount();
        if (activity.getCapacity() != null && enrolledCount >= activity.getCapacity()) {
            throw new BusinessException(409, "活动名额已满");
        }
        ActivityEnroll enroll = new ActivityEnroll();
        enroll.setActivityId(dto.getActivityId());
        enroll.setStudentId(userId);
        enroll.setStatus("ENROLLED");
        enroll.setDeleted(0);
        enroll.setCreateTime(LocalDateTime.now());
        enroll.setUpdateTime(LocalDateTime.now());
        activityEnrollMapper.insert(enroll);
        activity.setEnrolledCount(enrolledCount + 1);
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        evictActivityCaches();
        log(userId, "ENROLL_ACTIVITY", "ACTIVITY", dto.getActivityId(), "报名活动：" + activity.getTitle());
        notificationService.send(userId, "活动报名成功", "你已成功报名活动：" + activity.getTitle(), "ACTIVITY", activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnroll(Long activityId) {
        Long userId = requireLogin();
        ActivityEnroll enroll = activityEnrollMapper.selectOne(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getActivityId, activityId)
                .eq(ActivityEnroll::getStudentId, userId)
                .eq(ActivityEnroll::getStatus, "ENROLLED")
                .eq(ActivityEnroll::getDeleted, 0));
        if (enroll == null) {
            throw new BusinessException(404, "报名记录不存在");
        }
        enroll.setStatus("CANCELLED");
        enroll.setUpdateTime(LocalDateTime.now());
        activityEnrollMapper.updateById(enroll);
        Activity activity = activityMapper.selectById(activityId);
        if (activity != null && activity.getEnrolledCount() != null && activity.getEnrolledCount() > 0) {
            activity.setEnrolledCount(activity.getEnrolledCount() - 1);
            activity.setUpdateTime(LocalDateTime.now());
            activityMapper.updateById(activity);
        }
        evictActivityCaches();
        log(userId, "CANCEL_ACTIVITY_ENROLL", "ACTIVITY", activityId, "取消活动报名");
        notificationService.send(userId, "活动报名已取消", "你已取消活动报名。", "ACTIVITY", activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(CheckinDTO dto) {
        Long userId = requireLogin();
        Activity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null || activity.getDeleted() == 1) {
            throw new BusinessException(404, "活动不存在");
        }
        Long enrolled = activityEnrollMapper.selectCount(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getActivityId, dto.getActivityId())
                .eq(ActivityEnroll::getStudentId, userId)
                .eq(ActivityEnroll::getStatus, "ENROLLED")
                .eq(ActivityEnroll::getDeleted, 0));
        if (enrolled <= 0) {
            throw new BusinessException(403, "未报名该活动，不能签到");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getCheckinStartTime() != null && now.isBefore(activity.getCheckinStartTime())) {
            throw new BusinessException(409, "签到尚未开始");
        }
        if (activity.getCheckinEndTime() != null && now.isAfter(activity.getCheckinEndTime())) {
            throw new BusinessException(409, "签到已结束");
        }
        Long duplicated = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getActivityId, dto.getActivityId())
                .eq(Checkin::getStudentId, userId)
                .eq(Checkin::getDeleted, 0));
        if (duplicated > 0) {
            throw new BusinessException(409, "同一学生不能重复签到同一活动");
        }
        Checkin checkin = new Checkin();
        checkin.setActivityId(dto.getActivityId());
        checkin.setStudentId(userId);
        checkin.setCheckinTime(now);
        checkin.setDeleted(0);
        checkin.setCreateTime(now);
        checkin.setUpdateTime(now);
        checkinMapper.insert(checkin);
        evictActivityCaches();
        log(userId, "CHECKIN_ACTIVITY", "ACTIVITY", dto.getActivityId(), "活动签到：" + activity.getTitle());
        notificationService.send(userId, "活动签到成功", "你已完成活动签到：" + activity.getTitle(), "ACTIVITY", activity.getId());
    }

    @Override
    public Page<ActivityRecordVO> myEnrollments(Long current, Long size) {
        Long userId = requireLogin();
        Page<ActivityEnroll> page = activityEnrollMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getStudentId, userId)
                .eq(ActivityEnroll::getDeleted, 0)
                .orderByDesc(ActivityEnroll::getCreateTime));
        Set<Long> activityIds = page.getRecords().stream().map(ActivityEnroll::getActivityId).collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityIds.isEmpty()
                ? Collections.emptyMap()
                : activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity(), (left, right) -> left));
        Page<ActivityRecordVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(enroll -> {
            Activity activity = activityMap.get(enroll.getActivityId());
            return ActivityRecordVO.builder()
                    .id(enroll.getId())
                    .activityId(enroll.getActivityId())
                    .activityTitle(activity == null ? "-" : activity.getTitle())
                    .status(enroll.getStatus())
                    .enrollTime(enroll.getCreateTime())
                    .build();
        }).toList());
        return result;
    }

    @Override
    public Page<ActivityRecordVO> myCheckins(Long current, Long size) {
        Long userId = requireLogin();
        Page<Checkin> page = checkinMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getStudentId, userId)
                .eq(Checkin::getDeleted, 0)
                .orderByDesc(Checkin::getCreateTime));
        Set<Long> activityIds = page.getRecords().stream().map(Checkin::getActivityId).collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityIds.isEmpty()
                ? Collections.emptyMap()
                : activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity(), (left, right) -> left));
        Page<ActivityRecordVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(checkin -> {
            Activity activity = activityMap.get(checkin.getActivityId());
            return ActivityRecordVO.builder()
                    .id(checkin.getId())
                    .activityId(checkin.getActivityId())
                    .activityTitle(activity == null ? "-" : activity.getTitle())
                    .status("CHECKED_IN")
                    .checkinTime(checkin.getCheckinTime())
                    .build();
        }).toList());
        return result;
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    private void log(Long operatorId, String operation, String bizType, Long bizId, String detail) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperation(operation);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private void evictActivityCaches() {
        cacheEvictService.evictActivityCaches();
        cacheEvictService.evictRecommendationCaches();
        cacheEvictService.evictDashboardCaches();
    }

    private void fillActivity(Activity activity, ActivitySaveDTO dto, Venue venue) {
        activity.setTitle(dto.getTitle());
        activity.setVenueId(venue.getId());
        activity.setLocation(venue.getName() + "（" + venue.getLocation() + "）");
        activity.setCoverUrl(dto.getCoverUrl());
        activity.setContent(dto.getContent());
        activity.setCapacity(dto.getCapacity());
        activity.setScopeType(resolveScopeType(dto.getScopeType()));
        activity.setScopeCollege(resolveScopeCollege(dto.getScopeCollege()));
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setCheckinStartTime(dto.getCheckinStartTime());
        activity.setCheckinEndTime(dto.getCheckinEndTime());
        activity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }

    private void requireActivityOwner(Activity activity) {
        if (RoleUtils.hasAny("TEACHER") && !activity.getPublisherId().equals(UserContext.getUserId())) {
            throw new BusinessException(403, "教师只能管理自己发布的活动");
        }
    }

    private Venue requireVenue(Long venueId) {
        Venue venue = venueMapper.selectById(venueId);
        if (venue == null || venue.getDeleted() == 1 || venue.getStatus() == 0) {
            throw new BusinessException(404, "活动场地不存在或已停用");
        }
        return venue;
    }

    private void validateCapacity(ActivitySaveDTO dto, Venue venue) {
        if (dto.getCapacity() == null || dto.getCapacity() <= 0) {
            throw new BusinessException(400, "活动容量必须大于 0");
        }
        if (venue.getCapacity() != null && dto.getCapacity() > venue.getCapacity()) {
            throw new BusinessException(409, "活动容量不能超过所选场地容量");
        }
    }

    private void validateActivityTime(ActivitySaveDTO dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null && !dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException(400, "活动开始时间必须早于结束时间");
        }
        if (dto.getCheckinStartTime() != null && dto.getCheckinEndTime() != null && !dto.getCheckinStartTime().isBefore(dto.getCheckinEndTime())) {
            throw new BusinessException(400, "签到开始时间必须早于签到结束时间");
        }
        if (dto.getStartTime() != null && dto.getCheckinStartTime() != null && dto.getCheckinStartTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(400, "签到开始时间不能晚于活动开始时间");
        }
    }

    private String resolveScopeType(String scopeType) {
        if (RoleUtils.hasAny("ADMIN")) {
            return scopeType == null || scopeType.isBlank() ? "SCHOOL" : scopeType;
        }
        if ("SCHOOL".equals(scopeType)) {
            throw new BusinessException(403, "教师不能发布全校活动");
        }
        return "COLLEGE";
    }

    private String resolveScopeCollege(String scopeCollege) {
        String currentCollege = UserContext.get() == null ? null : UserContext.get().getCollege();
        if (RoleUtils.hasAny("ADMIN") && (scopeCollege == null || scopeCollege.isBlank())) {
            return null;
        }
        String college = scopeCollege == null || scopeCollege.isBlank() ? currentCollege : scopeCollege;
        if (!RoleUtils.hasAny("ADMIN") && (college == null || !college.equals(currentCollege))) {
            throw new BusinessException(403, "教师只能发布本院系活动");
        }
        return college;
    }

    private String currentRoleCode() {
        return UserContext.get() == null || UserContext.get().getRoleCode() == null
                ? "STUDENT"
                : UserContext.get().getRoleCode();
    }

    private String currentCollege() {
        return UserContext.get() == null ? null : UserContext.get().getCollege();
    }

    private String safeCollegeSegment(String college) {
        return college == null || college.isBlank() ? "none" : college;
    }

    private String safeUserSegment(Long userId) {
        return userId == null ? "guest" : String.valueOf(userId);
    }
}
