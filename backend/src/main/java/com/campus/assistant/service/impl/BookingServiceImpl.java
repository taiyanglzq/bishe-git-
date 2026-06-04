package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.AuditDTO;
import com.campus.assistant.dto.BookingCreateDTO;
import com.campus.assistant.entity.Booking;
import com.campus.assistant.entity.OperationLog;
import com.campus.assistant.entity.User;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.mapper.BookingMapper;
import com.campus.assistant.mapper.OperationLogMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.mapper.VenueSlotMapper;
import com.campus.assistant.service.BookingService;
import com.campus.assistant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final VenueMapper venueMapper;
    private final VenueSlotMapper venueSlotMapper;
    private final OperationLogMapper operationLogMapper;
    private final RedissonClient redissonClient;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BookingCreateDTO dto) {
        Long userId = requireLogin();
        RLock lock = redissonClient.getLock("ca:lock:booking:" + dto.getVenueId() + ":" + dto.getBookingDate() + ":" + dto.getTimeRange());
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(409, "当前预约请求较多，请稍后重试");
            }
            return createWithLock(dto, userId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "预约锁获取失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Long createWithLock(BookingCreateDTO dto, Long userId) {
        Venue venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null || venue.getStatus() == 0) {
            throw new BusinessException(404, "场地不存在或不可预约");
        }
        VenueSlot slot = venueSlotMapper.selectOne(new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getVenueId, dto.getVenueId())
                .eq(VenueSlot::getSlotDate, dto.getBookingDate())
                .eq(VenueSlot::getTimeRange, dto.getTimeRange())
                .eq(VenueSlot::getDeleted, 0));
        if (slot == null || slot.getStatus() == 0) {
            throw new BusinessException(404, "该场地当前时间段未开放预约");
        }
        if (slot.getRemainingQuota() == null || slot.getRemainingQuota() <= 0) {
            throw new BusinessException(409, "该时间段剩余名额不足");
        }
        Long duplicated = bookingMapper.selectCount(new LambdaQueryWrapper<Booking>()
                .eq(Booking::getStudentId, userId)
                .eq(Booking::getVenueId, dto.getVenueId())
                .eq(Booking::getBookingDate, dto.getBookingDate())
                .eq(Booking::getTimeRange, dto.getTimeRange())
                .in(Booking::getStatus, "PENDING", "APPROVED")
                .eq(Booking::getDeleted, 0));
        if (duplicated > 0) {
            throw new BusinessException(409, "不能重复预约同一场地的同一时间段哦！");
        }
        slot.setRemainingQuota(slot.getRemainingQuota() - 1);
        slot.setUpdateTime(LocalDateTime.now());
        venueSlotMapper.updateById(slot);
        Booking booking = new Booking();
        booking.setStudentId(userId);
        booking.setVenueId(dto.getVenueId());
        booking.setBookingDate(dto.getBookingDate());
        booking.setTimeRange(dto.getTimeRange());
        booking.setReason(dto.getReason());
        booking.setStatus("PENDING");
        booking.setDeleted(0);
        booking.setCreateTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.insert(booking);
        log(userId, "CREATE_BOOKING", "BOOKING", booking.getId(), "提交场地预约申请");
        notificationService.send(userId, "预约提交成功", "你的场地预约已提交，当前状态为待审核。", "BOOKING", booking.getId());
        return booking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(AuditDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        audit(dto, "APPROVED", "审核通过预约");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(AuditDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        audit(dto, "REJECTED", "审核驳回预约");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long bookingId) {
        Long userId = requireLogin();
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (!booking.getStudentId().equals(userId)) {
            throw new BusinessException(403, "不能取消他人的预约");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BusinessException(409, "预约已取消，请勿重复操作");
        }
        if ("PENDING".equals(booking.getStatus()) || "APPROVED".equals(booking.getStatus())) {
            releaseSlot(booking);
        }
        booking.setStatus("CANCELLED");
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log(userId, "CANCEL_BOOKING", "BOOKING", bookingId, "取消场地预约");
        notificationService.send(userId, "预约已取消", "你已取消该场地预约，系统已释放对应时间段名额。", "BOOKING", bookingId);
    }

    @Override
    public Page<Booking> page(Long current, Long size, String status) {
        Long userId = requireLogin();
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<Booking>()
                .eq(Booking::getDeleted, 0)
                .eq(status != null && !status.isBlank(), Booking::getStatus, status)
                .orderByDesc(Booking::getCreateTime);
        if (!RoleUtils.hasAny("TEACHER", "ADMIN")) {
            wrapper.eq(Booking::getStudentId, userId);
        } else if (RoleUtils.hasAny("TEACHER")) {
            User teacher = UserContext.get();
            var studentIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .eq(User::getRoleCode, "STUDENT")
                    .eq(User::getCollege, teacher.getCollege())
                    .eq(User::getDeleted, 0))
                    .stream()
                    .map(User::getId)
                    .toList();
            if (studentIds.isEmpty()) {
                return Page.of(current, size, 0);
            }
            wrapper.in(Booking::getStudentId, studentIds);
        }
        return bookingMapper.selectPage(Page.of(current, size), wrapper);
    }

    private void audit(AuditDTO dto, String status, String operation) {
        Long operatorId = requireLogin();
        Booking booking = bookingMapper.selectById(dto.getBookingId());
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException(409, "该预约已处理，不能重复审核");
        }
        if (RoleUtils.hasAny("TEACHER")) {
            User teacher = UserContext.get();
            User student = userMapper.selectById(booking.getStudentId());
            if (student == null || teacher.getCollege() == null || !teacher.getCollege().equals(student.getCollege())) {
                throw new BusinessException(403, "教师只能审核本院系学生预约");
            }
        }
        if ("REJECTED".equals(status)) {
            releaseSlot(booking);
        }
        booking.setStatus(status);
        booking.setAuditUserId(operatorId);
        booking.setAuditRemark(dto.getRemark());
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.updateById(booking);
        log(operatorId, operation, "BOOKING", booking.getId(), dto.getRemark());
        String title = "APPROVED".equals(status) ? "预约审核通过" : "预约审核驳回";
        String content = "APPROVED".equals(status) ? "你的场地预约已审核通过，请按时使用场地。" : "你的场地预约未通过审核，原因：" + (dto.getRemark() == null ? "未填写" : dto.getRemark());
        notificationService.send(booking.getStudentId(), title, content, "BOOKING", booking.getId());
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

    private void releaseSlot(Booking booking) {
        VenueSlot slot = venueSlotMapper.selectOne(new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getVenueId, booking.getVenueId())
                .eq(VenueSlot::getSlotDate, booking.getBookingDate())
                .eq(VenueSlot::getTimeRange, booking.getTimeRange())
                .eq(VenueSlot::getDeleted, 0));
        if (slot == null) {
            return;
        }
        int totalQuota = slot.getTotalQuota() == null ? Integer.MAX_VALUE : slot.getTotalQuota();
        int currentRemaining = slot.getRemainingQuota() == null ? 0 : slot.getRemainingQuota();
        slot.setRemainingQuota(Math.min(totalQuota, currentRemaining + 1));
        slot.setUpdateTime(LocalDateTime.now());
        venueSlotMapper.updateById(slot);
    }
}
