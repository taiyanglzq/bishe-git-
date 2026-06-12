package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.BookBorrowDTO;
import com.campus.assistant.dto.BookSaveDTO;
import com.campus.assistant.entity.Book;
import com.campus.assistant.entity.BookBorrow;

import java.util.List;

/**
 * 图书服务接口，定义图书检索和后台管理相关业务能力。
 */
public interface BookService {

    /**
     * 图书分页检索，支持按分类、关键字搜索
     */
    Page<Book> page(Long current, Long size, String category, String keyword);

    /**
     * 图书详情
     */
    Book detail(Long id);

    /**
     * 管理端图书分页
     */
    Page<Book> managePage(Long current, Long size);

    /**
     * 新增图书
     */
    Long save(BookSaveDTO dto);

    /**
     * 编辑图书
     */
    void update(BookSaveDTO dto);

    /**
     * 删除图书
     */
    void delete(Long id);

    /**
     * 借阅图书，可用数量减 1 并创建借阅记录
     */
    void borrow(BookBorrowDTO dto);

    /**
     * 归还图书，可用数量加 1 并更新借阅记录状态
     */
    void returnBook(Long bookId);

    /**
     * 获取当前用户的借阅记录
     */
    List<BookBorrow> myBorrows();
}
