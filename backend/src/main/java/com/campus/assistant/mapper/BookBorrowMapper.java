package com.campus.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.assistant.entity.BookBorrow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图书借阅 Mapper
 */
@Mapper
public interface BookBorrowMapper extends BaseMapper<BookBorrow> {
}
