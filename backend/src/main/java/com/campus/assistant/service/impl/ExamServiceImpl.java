package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.ExamSaveDTO;
import com.campus.assistant.dto.ExamSeatGenerateDTO;
import com.campus.assistant.entity.Exam;
import com.campus.assistant.entity.ExamSeat;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.ExamMapper;
import com.campus.assistant.mapper.ExamSeatMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.DelayTaskService;
import com.campus.assistant.service.ExamService;
import com.campus.assistant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 考试服务实现，负责考试分页、详情、后台管理、座位安排以及考试通知和考前提醒。
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final UserMapper userMapper;
    private final ExamSeatMapper examSeatMapper;
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
        exam.setInvigilator(dto.getInvigilator());
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
        exam.setInvigilator(dto.getInvigilator());
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
            examMapper.update(null, new LambdaUpdateWrapper<Exam>()
                    .eq(Exam::getId, id)
                    .set(Exam::getDeleted, 1)
                    .set(Exam::getUpdateTime, LocalDateTime.now()));
            // 一并删除座位
            examSeatMapper.update(null, new LambdaUpdateWrapper<ExamSeat>()
                    .eq(ExamSeat::getExamId, id)
                    .set(ExamSeat::getDeleted, 1));
        }
    }

    // ========== 座位管理 ==========

    @Override
    public List<ExamSeat> getSeats(Long examId) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        return examSeatMapper.selectList(new LambdaQueryWrapper<ExamSeat>()
                .eq(ExamSeat::getExamId, examId)
                .eq(ExamSeat::getDeleted, 0)
                .orderByAsc(ExamSeat::getSeatNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateSeats(Long examId, String mode) {
        RoleUtils.requireAny("ADMIN");
        Exam exam = examMapper.selectById(examId);
        if (exam == null || exam.getDeleted() == 1) {
            throw new BusinessException(404, "考试不存在");
        }
        // 删除旧座位
        examSeatMapper.update(null, new LambdaUpdateWrapper<ExamSeat>()
                .eq(ExamSeat::getExamId, examId)
                .set(ExamSeat::getDeleted, 1));

        // 获取同院系学生
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getCollege, exam.getCollege())
                .eq(User::getRoleCode, "STUDENT")
                .eq(User::getStatus, 1)
                .eq(User::getDeleted, 0));
        if (students.isEmpty()) {
            throw new BusinessException(409, "该院系没有学生");
        }

        List<ExamSeat> seats;
        switch (mode) {
            case "STUDENT_NO":
                students.sort((a, b) -> {
                    String snoA = a.getStudentNo() == null ? "" : a.getStudentNo();
                    String snoB = b.getStudentNo() == null ? "" : b.getStudentNo();
                    return snoA.compareTo(snoB);
                });
                seats = assignSequential(examId, students);
                break;
            case "RANDOM":
                Collections.shuffle(students);
                seats = assignSequential(examId, students);
                break;
            default: // CLASSROOM
                seats = assignClassroom(examId, students);
                break;
        }

        for (ExamSeat seat : seats) {
            examSeatMapper.insert(seat);
        }
    }

    private List<ExamSeat> assignSequential(Long examId, List<User> students) {
        List<ExamSeat> seats = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            User s = students.get(i);
            ExamSeat seat = new ExamSeat();
            seat.setExamId(examId);
            seat.setStudentId(s.getId());
            seat.setStudentName(s.getRealName());
            seat.setStudentNo(s.getStudentNo());
            seat.setSeatNo(String.valueOf(i + 1));
            seat.setCollege(s.getCollege());
            seat.setDeleted(0);
            seat.setCreateTime(LocalDateTime.now());
            seat.setUpdateTime(LocalDateTime.now());
            seats.add(seat);
        }
        return seats;
    }

    private List<ExamSeat> assignClassroom(Long examId, List<User> students) {
        List<ExamSeat> seats = new ArrayList<>();
        int cols = 10;
        for (int i = 0; i < students.size(); i++) {
            User s = students.get(i);
            int row = i / cols;
            int col = i % cols;
            char rowLetter = (char) ('A' + row);
            String seatNo = String.format("%c%d", rowLetter, col + 1);
            ExamSeat seat = new ExamSeat();
            seat.setExamId(examId);
            seat.setStudentId(s.getId());
            seat.setStudentName(s.getRealName());
            seat.setStudentNo(s.getStudentNo());
            seat.setSeatNo(seatNo);
            seat.setCollege(s.getCollege());
            seat.setDeleted(0);
            seat.setCreateTime(LocalDateTime.now());
            seat.setUpdateTime(LocalDateTime.now());
            seats.add(seat);
        }
        return seats;
    }

    @Override
    public void updateSeat(Long seatId, String seatNo) {
        RoleUtils.requireAny("ADMIN");
        ExamSeat seat = examSeatMapper.selectById(seatId);
        if (seat == null || seat.getDeleted() == 1) {
            throw new BusinessException(404, "座位记录不存在");
        }
        seat.setSeatNo(seatNo);
        seat.setUpdateTime(LocalDateTime.now());
        examSeatMapper.updateById(seat);
    }

    @Override
    public byte[] exportSeats(Long examId) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(404, "考试不存在");
        }
        List<ExamSeat> seats = getSeats(examId);

        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        Sheet sheet = wb.createSheet("考场座位表");

        // 标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(exam.getCourseName() + " 考场座位表（" + exam.getExamDate() + " " + exam.getStartTime() + "-" + exam.getEndTime() + "）");
        titleCell.setCellStyle(boldStyle(wb));

        // 表头
        String[] headers = {"序号", "学号", "姓名", "院系", "座位号"};
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle(wb));
        }

        // 数据行
        for (int i = 0; i < seats.size(); i++) {
            ExamSeat s = seats.get(i);
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(s.getStudentNo() == null ? "" : s.getStudentNo());
            row.createCell(2).setCellValue(s.getStudentName() == null ? "" : s.getStudentName());
            row.createCell(3).setCellValue(s.getCollege() == null ? "" : s.getCollege());
            row.createCell(4).setCellValue(s.getSeatNo() == null ? "" : s.getSeatNo());
        }

        // 自动列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            wb.write(out);
            wb.dispose();
        } catch (Exception e) {
            throw new BusinessException(500, "导出座位表失败：" + e.getMessage());
        }
        return out.toByteArray();
    }

    private CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
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
                    + (exam.getInvigilator() != null ? "监考老师：" + exam.getInvigilator() : "")
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
