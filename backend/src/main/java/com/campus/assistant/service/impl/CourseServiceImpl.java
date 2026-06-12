package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.CourseSaveDTO;
import com.campus.assistant.entity.Course;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.CourseMapper;
import com.campus.assistant.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 课程服务实现，负责课程分页、详情和后台管理逻辑。
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

    @Override
    public Page<Course> page(Long current, Long size, String college, String semester, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getDeleted, 0)
                .eq(Course::getStatus, 1);
        if (StringUtils.hasText(college)) {
            wrapper.eq(Course::getCollege, college);
        }
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Course::getSemester, semester);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getName, keyword)
                    .or()
                    .like(Course::getTeacherName, keyword));
        }
        wrapper.orderByAsc(Course::getCollege, Course::getSemester, Course::getName);
        return courseMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Course detail(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || course.getDeleted() == 1) {
            throw new BusinessException(404, "课程不存在");
        }
        return course;
    }

    @Override
    public Page<Course> managePage(Long current, Long size) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getDeleted, 0);
        if (RoleUtils.hasAny("TEACHER")) {
            User currentUser = UserContext.get();
            if (currentUser != null && StringUtils.hasText(currentUser.getCollege())) {
                wrapper.eq(Course::getCollege, currentUser.getCollege());
            }
        }
        wrapper.orderByDesc(Course::getCreateTime);
        return courseMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Long save(CourseSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Course course = new Course();
        course.setName(dto.getName());
        course.setTeacherName(dto.getTeacherName());
        course.setCollege(dto.getCollege());
        course.setSemester(dto.getSemester());
        course.setClassroom(dto.getClassroom());
        course.setScheduleInfo(dto.getScheduleInfo());
        course.setCredit(dto.getCredit());
        course.setCapacity(dto.getCapacity());
        course.setDescription(dto.getDescription());
        course.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        course.setDeleted(0);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    public void update(CourseSaveDTO dto) {
        RoleUtils.requireAny("TEACHER", "ADMIN");
        Course course = courseMapper.selectById(dto.getId());
        if (course == null || course.getDeleted() == 1) {
            throw new BusinessException(404, "课程不存在");
        }
        course.setName(dto.getName());
        course.setTeacherName(dto.getTeacherName());
        course.setCollege(dto.getCollege());
        course.setSemester(dto.getSemester());
        course.setClassroom(dto.getClassroom());
        course.setScheduleInfo(dto.getScheduleInfo());
        course.setCredit(dto.getCredit());
        course.setCapacity(dto.getCapacity());
        course.setDescription(dto.getDescription());
        course.setStatus(dto.getStatus() == null ? course.getStatus() : dto.getStatus());
        course.setUpdateTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        Course course = courseMapper.selectById(id);
        if (course != null) {
            courseMapper.deleteById(id);
        }
    }
}
