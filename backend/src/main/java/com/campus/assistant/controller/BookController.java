package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.BookBorrowDTO;
import com.campus.assistant.dto.BookSaveDTO;
import com.campus.assistant.entity.Book;
import com.campus.assistant.entity.BookBorrow;
import com.campus.assistant.vo.BookBorrowVO;

import java.util.List;
import com.campus.assistant.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图书控制器，负责接收图书检索和后台管理请求并调用图书服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;

    @GetMapping("/page")
    public Result<Page<Book>> page(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "12") Long size,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String keyword) {
        return Result.success(bookService.page(current, size, category, keyword));
    }

    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Long id) {
        return Result.success(bookService.detail(id));
    }

    @GetMapping("/manage/page")
    public Result<Page<Book>> managePage(@RequestParam(defaultValue = "1") Long current,
                                          @RequestParam(defaultValue = "10") Long size) {
        return Result.success(bookService.managePage(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody BookSaveDTO dto) {
        return Result.success(bookService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody BookSaveDTO dto) {
        bookService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success();
    }

    @PostMapping("/borrow")
    public Result<Void> borrow(@Valid @RequestBody BookBorrowDTO dto) {
        bookService.borrow(dto);
        return Result.success();
    }

    @PostMapping("/return/{bookId}")
    public Result<Void> returnBook(@PathVariable Long bookId) {
        bookService.returnBook(bookId);
        return Result.success();
    }

    @GetMapping("/my-borrows")
    public Result<List<BookBorrow>> myBorrows() {
        return Result.success(bookService.myBorrows());
    }

    @GetMapping("/borrow/page")
    public Result<Page<BookBorrowVO>> borrowPage(@RequestParam(defaultValue = "1") Long current,
                                                  @RequestParam(defaultValue = "10") Long size) {
        return Result.success(bookService.borrowPage(current, size));
    }
}
