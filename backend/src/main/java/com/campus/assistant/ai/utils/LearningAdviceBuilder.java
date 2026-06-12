package com.campus.assistant.ai.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.entity.*;
import com.campus.assistant.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习建议上下文构建器，根据用户数据构建个性化学习建议 prompt。
 */
@Component
@RequiredArgsConstructor
public class LearningAdviceBuilder {

    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final ActivityEnrollMapper activityEnrollMapper;
    private final CheckinMapper checkinMapper;

    /**
     * 构建个性化学习建议的系统提示词
     */
    public String buildAdvicePrompt(User user, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位校园学习顾问，请根据以下学生数据提供个性化学习建议。\n\n");

        if (user.getCollege() != null) {
            // 该学生的课程信息
            List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getCollege, user.getCollege())
                    .eq(Course::getDeleted, 0)
                    .eq(Course::getStatus, 1));
            if (!courses.isEmpty()) {
                sb.append("【学生课程列表】\n");
                for (Course c : courses) {
                    sb.append("- ").append(c.getName())
                            .append("（学分：").append(c.getCredit())
                            .append("，教师：").append(c.getTeacherName())
                            .append("，时间：").append(c.getScheduleInfo()).append("）\n");
                }
                sb.append("\n");
            }

            // 最近的考试安排
            List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .eq(Exam::getCollege, user.getCollege())
                    .eq(Exam::getDeleted, 0)
                    .eq(Exam::getStatus, 1)
                    .ge(Exam::getExamDate, LocalDate.now())
                    .orderByAsc(Exam::getExamDate));
            if (!exams.isEmpty()) {
                sb.append("【即将到来的考试】\n");
                for (Exam e : exams) {
                    sb.append("- ").append(e.getCourseName())
                            .append("（").append(e.getExamType())
                            .append("，日期：").append(e.getExamDate())
                            .append("，地点：").append(e.getLocation()).append("）\n");
                }
                sb.append("\n");
            }
        }

        // 学生的活动签到统计
        Long enrolledCount = activityEnrollMapper.selectCount(new LambdaQueryWrapper<ActivityEnroll>()
                .eq(ActivityEnroll::getStudentId, user.getId())
                .eq(ActivityEnroll::getDeleted, 0));
        Long checkinCount = checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getStudentId, user.getId())
                .eq(Checkin::getDeleted, 0));
        sb.append("【学生活动参与】已报名 ").append(enrolledCount)
                .append(" 个活动，已签到 ").append(checkinCount).append(" 次。\n\n");

        String adviceType = type != null ? type : "study_plan";
        switch (adviceType) {
            case "exam_prep":
                sb.append("请基于以上数据，为该学生制定期末考试复习计划，包括时间安排、科目优先级和复习方法建议。");
                break;
            case "weak_subject":
                sb.append("请根据课程列表和考试安排，分析学生可能存在的薄弱环节，并给出针对性学习建议。");
                break;
            default:
                sb.append("请基于以上数据，为该学生制定下一阶段的学习计划，包括每周学习安排、重点科目和备考建议。");
                break;
        }

        return sb.toString();
    }
}
