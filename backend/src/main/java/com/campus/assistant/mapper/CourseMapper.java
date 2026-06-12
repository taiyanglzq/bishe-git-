package com.campus.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.assistant.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程 Mapper，负责 ca_course 表的数据库操作
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
