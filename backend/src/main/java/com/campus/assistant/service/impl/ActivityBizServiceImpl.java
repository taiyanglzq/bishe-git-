package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.ActivityEnrollDTO;
import com.campus.assistant.dto.CheckinDTO;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.ActivityEnroll;
import com.campus.assistant.entity.Checkin;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.mapper.ActivityEnrollMapper;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.CheckinMapper;
import com.campus.assistant.mapper.OperationLogMapper;
import com.campus.assistant.service.ActivityBizService;
import com.campus.assistant.service.NotificationService;
import com.campus.assistant.vo.ActivityRecordVO;
import com.campus.assistant.vo.ActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityBizServiceImpl implements ActivityBizService {

    private final ActivityMapper activityMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final CheckinMapper checkinMapper;
    private final OperationLogMapper operationLogMapper;
    private final NotificationService notificationService;

    @Override
    public Page<ActivityVO> page(Long current, Long size) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0);
        if (!RoleUtils.hasAny("ADMIN")) {
            String college = UserContext.get() == null ? null : UserContext.get().getCollege();
            wrapper.and(query -> query.eq(Activity::getScopeType, "SCHOOL")
                    .or()
                    .eq(Activity::getScopeCollege, college));
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        Page<Activity> page = activityMapper.selectPage(Page.of(current, size), wrapper);
        Page<ActivityVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(activity -> {
            boolean enrolled = false;
            boolean checkedIn = false;
            if (userId != null) {
                enrolled = activityEnrollMapper.selectCount(new LambdaQueryWrapper<ActivityEnroll>()
                        .eq(ActivityEnroll::getActivityId, activity.getId())
                        .eq(ActivityEnroll::getStudentId, userId)
                        .eq(ActivityEnroll::getStatus, "ENROLLED")
                        .eq(ActivityEnroll::getDeleted, 0)) > 0;
                checkedIn = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                        .eq(Checkin::getActivityId, activity.getId())
                        .eq(Checkin::getStudentId, userId)
                        .eq(Checkin::getDeleted, 0)) > 0;
            }
            return ActivityVO.from(activity, enrolled, checkedIn);
        }).toList());
        return result;
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
        Page<ActivityRecordVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(enroll -> {
            Activity activity = activityMapper.selectById(enroll.getActivityId());
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
        Page<ActivityRecordVO> result = Page.of(current, size, page.getTotal());
        result.setRecords(page.getRecords().stream().map(checkin -> {
            Activity activity = activityMapper.selectById(checkin.getActivityId());
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
}
