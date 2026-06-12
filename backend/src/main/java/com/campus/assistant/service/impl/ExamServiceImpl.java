package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.ExamSaveDTO;
import com.campus.assistant.entity.Exam;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.ExamMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.DelayTaskService;
import com.campus.assistant.service.ExamService;
import com.campus.assistant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 考试服务实现，负责考试分页、详情、后台管理以及考试通知和考前提醒。
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final DelayTaskService delayTaskService;

    private static final String EXAM_REMIND_QUEUE = "ca:delay:exam:remind";

    @Override
    public Page<Exam> page(Long current, Long size, String college, String examType, String keyword) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<Exam>()
                .eq(Exam::getDeleted, 0)
                .eq(Exam::getStatus, 1);
        if (StringUtils.hasText(college)) {
            wrapper.eq(Exam::getCollege, college);
        }
        if (StringUtils.hasText(examType)) {
            wrapper.eq(Exam::getExamType, examType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Exam::getCourseName, keyword);
        }
        wrapper.orderByAsc(Exam::getExamDate, Exam::getStartTime);
        return examMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Exam detail(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException(404, "考试安排不存在");
        }
        return exam;
    }

    @Override
    public Page<Exam> managePage(Long current, Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<Exam>()
                .eq(Exam::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            User currentUser = UserContext.get();
            if (currentUser != null && StringUtils.hasText(currentUser.getCollege())) {
                wrapper.eq(Exam::getCollege, currentUser.getCollege());
            }
        }
        wrapper.orderByDesc(Exam::getCreateTime);
        return examMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Long save(ExamSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        validateExamTime(dto);
        Exam exam = new Exam();
        exam.setCourseId(dto.getCourseId());
        exam.setCourseName(dto.getCourseName());
        exam.setExamDate(dto.getExamDate());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setLocation(dto.getLocation());
        exam.setSeatNo(dto.getSeatNo());
        exam.setExamType(dto.getExamType());
        exam.setCollege(dto.getCollege());
        exam.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        exam.setDeleted(0);
        exam.setCreateTime(LocalDateTime.now());
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.insert(exam);

        // 通知同院系学生
        sendExamNotification(exam);

        // 考前延迟提醒
        scheduleExamReminder(exam);

        return exam.getId();
    }

    @Override
    public void update(ExamSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        validateExamTime(dto);
        Exam exam = examMapper.selectById(dto.getId());
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException(404, "考试安排不存在");
        }
        exam.setCourseId(dto.getCourseId());
        exam.setCourseName(dto.getCourseName());
        exam.setExamDate(dto.getExamDate());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setLocation(dto.getLocation());
        exam.setSeatNo(dto.getSeatNo());
        exam.setExamType(dto.getExamType());
        exam.setCollege(dto.getCollege());
        exam.setStatus(dto.getStatus() == null ? exam.getStatus() : dto.getStatus());
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(exam);
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        Exam exam = examMapper.selectById(id);
        if (exam != null) {
            examMapper.deleteById(id);
        }
    }

    /**
     * 向同院系学生发送考试通知
     */
    private void sendExamNotification(Exam exam) {
        if (exam.getCollege() == null || exam.getCollege().isBlank()) {
            return;
        }
        try {
            List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .eq(User::getCollege, exam.getCollege())
                    .eq(User::getRoleCode, "STUDENT")
                    .eq(User::getStatus, 1)
                    .eq(User::getDeleted, 0));
            String title = "考试通知：" + exam.getCourseName();
            String content = exam.getCourseName() + " " + exam.getExamType()
                    + " 将于 " + exam.getExamDate() + " " + exam.getStartTime() + "-" + exam.getEndTime()
                    + " 在 " + (exam.getLocation() != null ? exam.getLocation() : "待定") + " 举行。"
                    + (exam.getSeatNo() != null ? "座位号：" + exam.getSeatNo() : "");
            for (User student : students) {
                notificationService.send(student.getId(), title, content, "EXAM", exam.getId());
            }
        } catch (Exception e) {
            // 通知发送失败不影响考试创建
        }
    }

    /**
     * 考前延迟提醒：考试前一天发送提醒通知
     */
    private void scheduleExamReminder(Exam exam) {
        if (exam.getExamDate() == null || exam.getStartTime() == null) {
            return;
        }
        try {
            LocalDateTime examDateTime = LocalDateTime.of(exam.getExamDate(),
                    exam.getStartTime() != null ? exam.getStartTime() : LocalTime.of(9, 0));
            LocalDateTime remindTime = examDateTime.minusDays(1);
            LocalDateTime now = LocalDateTime.now();
            if (remindTime.isAfter(now)) {
                Duration delay = Duration.between(now, remindTime);
                String taskBody = exam.getCourseName() + "|" + exam.getExamDate() + "|" + exam.getExamType() + "|" + exam.getCollege();
                delayTaskService.addTask(EXAM_REMIND_QUEUE, taskBody, delay);
            }
        } catch (Exception e) {
            // 延迟任务投递失败不影响考试创建
        }
    }

    private void validateExamTime(ExamSaveDTO dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null
                && !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(400, "考试结束时间必须晚于开始时间");
        }
    }
}
