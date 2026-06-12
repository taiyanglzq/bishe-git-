package com.campus.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.assistant.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试 Mapper，负责 ca_exam 表的数据库操作
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
