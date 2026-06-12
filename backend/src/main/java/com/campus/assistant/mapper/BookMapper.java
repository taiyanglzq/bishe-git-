package com.campus.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.assistant.entity.Book;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书 Mapper，负责 ca_book 表的数据库操作
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
