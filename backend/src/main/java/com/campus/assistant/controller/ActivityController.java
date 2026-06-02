package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.ActivitySaveDTO;
import com.campus.assistant.dto.ActivityEnrollDTO;
import com.campus.assistant.entity.Activity;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.mapper.ActivityMapper;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.service.ActivityBizService;
import com.campus.assistant.vo.ActivityRecordVO;
import com.campus.assistant.vo.ActivityVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {

    private final ActivityBizService activityBizService;
    private final ActivityMapper activityMapper;
    private final VenueMapper venueMapper;

    @GetMapping("/page")
    public Result<Page<ActivityVO>> page(@RequestParam(defaultValue = "1") Long current,
                                         @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.page(current, size));
    }

    @PostMapping("/enroll")
    public Result<Void> enroll(@Valid @RequestBody ActivityEnrollDTO dto) {
        activityBizService.enroll(dto);
        return Result.success();
    }

    @PostMapping("/cancel/{activityId}")
    public Result<Void> cancel(@PathVariable Long activityId) {
        activityBizService.cancelEnroll(activityId);
        return Result.success();
    }

    @GetMapping("/my-enrollments")
    public Result<Page<ActivityRecordVO>> myEnrollments(@RequestParam(defaultValue = "1") Long current,
                                                       @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.myEnrollments(current, size));
    }

    @GetMapping("/my-checkins")
    public Result<Page<ActivityRecordVO>> myCheckins(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam(defaultValue = "10") Long size) {
        return Result.success(activityBizService.myCheckins(current, size));
    }

    @GetMapping("/manage/page")
    public Result<Page<Activity>> managePage(@RequestParam(defaultValue = "1") Long current,
                                             @RequestParam(defaultValue = "10") Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Activity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Activity>()
                .eq(Activity::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            wrapper.eq(Activity::getPublisherId, UserContext.getUserId());
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        return Result.success(activityMapper.selectPage(Page.of(current, size), wrapper));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody ActivitySaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Activity activity = new Activity();
        Venue venue = requireVenue(dto.getVenueId());
        validateActivityTime(dto);
        validateCapacity(dto, venue);
        activity.setTitle(dto.getTitle());
        activity.setVenueId(venue.getId());
        activity.setLocation(venue.getName() + "（" + venue.getLocation() + "）");
        activity.setContent(dto.getContent());
        activity.setCapacity(dto.getCapacity());
        activity.setEnrolledCount(0);
        activity.setPublisherId(UserContext.getUserId());
        activity.setScopeType(resolveScopeType(dto.getScopeType()));
        activity.setScopeCollege(resolveScopeCollege(dto.getScopeCollege()));
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setCheckinStartTime(dto.getCheckinStartTime());
        activity.setCheckinEndTime(dto.getCheckinEndTime());
        activity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        activity.setDeleted(0);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.insert(activity);
        return Result.success(activity.getId());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ActivitySaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Activity activity = activityMapper.selectById(dto.getId());
        if (activity == null) {
            return Result.fail(404, "活动不存在");
        }
        requireActivityOwner(activity);
        Venue venue = requireVenue(dto.getVenueId());
        validateActivityTime(dto);
        validateCapacity(dto, venue);
        activity.setTitle(dto.getTitle());
        activity.setVenueId(venue.getId());
        activity.setLocation(venue.getName() + "（" + venue.getLocation() + "）");
        activity.setContent(dto.getContent());
        activity.setCapacity(dto.getCapacity());
        activity.setScopeType(resolveScopeType(dto.getScopeType()));
        activity.setScopeCollege(resolveScopeCollege(dto.getScopeCollege()));
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setCheckinStartTime(dto.getCheckinStartTime());
        activity.setCheckinEndTime(dto.getCheckinEndTime());
        activity.setStatus(dto.getStatus() == null ? activity.getStatus() : dto.getStatus());
        activity.setUpdateTime(LocalDateTime.now());
        activityMapper.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Activity activity = activityMapper.selectById(id);
        if (activity != null) {
            requireActivityOwner(activity);
            activity.setDeleted(1);
            activity.setUpdateTime(LocalDateTime.now());
            activityMapper.updateById(activity);
        }
        return Result.success();
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
        if (dto.getCapacity() <= 0) {
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
        if (RoleUtils.hasAny("ADMIN") && "SCHOOL".equals(scopeCollege)) {
            return null;
        }
        String college = scopeCollege == null || scopeCollege.isBlank() ? currentCollege : scopeCollege;
        if (!RoleUtils.hasAny("ADMIN") && (college == null || !college.equals(currentCollege))) {
            throw new BusinessException(403, "教师只能发布本院系活动");
        }
        return college;
    }
}
