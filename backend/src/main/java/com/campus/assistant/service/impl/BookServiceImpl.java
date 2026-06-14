package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.BookBorrowDTO;
import com.campus.assistant.dto.BookSaveDTO;
import com.campus.assistant.entity.Book;
import com.campus.assistant.entity.BookBorrow;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.BookBorrowMapper;
import com.campus.assistant.mapper.BookMapper;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.BookService;
import com.campus.assistant.vo.BookBorrowVO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图书服务实现，负责图书检索、详情和后台管理逻辑。
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookBorrowMapper bookBorrowMapper;
    private final UserMapper userMapper;
    private final RedissonClient redissonClient;

    @Override
    public Page<Book> page(Long current, Long size, String category, String keyword) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                .eq(Book::getDeleted, 0)
                .eq(Book::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or()
                    .like(Book::getAuthor, keyword)
                    .or()
                    .like(Book::getIsbn, keyword));
        }
        wrapper.orderByAsc(Book::getCategory, Book::getTitle);
        return bookMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Book detail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null || book.getDeleted() == 1) {
            throw new BusinessException(404, "图书不存在");
        }
        return book;
    }

    @Override
    public Page<Book> managePage(Long current, Long size) {
        RoleUtils.requireAny("ADMIN");
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                .eq(Book::getDeleted, 0)
                .orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(Page.of(current, size), wrapper);
    }

    @Override
    public Long save(BookSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublishYear(dto.getPublishYear());
        book.setCategory(dto.getCategory());
        book.setLocation(dto.getLocation());
        book.setTotalCount(dto.getTotalCount() == null ? 1 : dto.getTotalCount());
        book.setAvailableCount(dto.getAvailableCount() == null ? book.getTotalCount() : dto.getAvailableCount());
        book.setDescription(dto.getDescription());
        book.setCoverUrl(dto.getCoverUrl());
        book.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        book.setDeleted(0);
        book.setCreateTime(LocalDateTime.now());
        book.setUpdateTime(LocalDateTime.now());
        bookMapper.insert(book);
        return book.getId();
    }

    @Override
    public void update(BookSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Book book = bookMapper.selectById(dto.getId());
        if (book == null || book.getDeleted() == 1) {
            throw new BusinessException(404, "图书不存在");
        }
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublisher(dto.getPublisher());
        book.setPublishYear(dto.getPublishYear());
        book.setCategory(dto.getCategory());
        book.setLocation(dto.getLocation());
        book.setTotalCount(dto.getTotalCount() == null ? book.getTotalCount() : dto.getTotalCount());
        book.setAvailableCount(dto.getAvailableCount() == null ? book.getAvailableCount() : dto.getAvailableCount());
        book.setDescription(dto.getDescription());
        book.setCoverUrl(dto.getCoverUrl());
        book.setStatus(dto.getStatus() == null ? book.getStatus() : dto.getStatus());
        book.setUpdateTime(LocalDateTime.now());
        bookMapper.updateById(book);
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        Book book = bookMapper.selectById(id);
        if (book != null) {
            bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                    .eq(Book::getId, id)
                    .set(Book::getDeleted, 1)
                    .set(Book::getUpdateTime, LocalDateTime.now()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrow(BookBorrowDTO dto) {
        Long userId = requireLogin();

        RLock lock = redissonClient.getLock("ca:lock:borrow:" + dto.getBookId());
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(409, "当前借阅请求较多，请稍后重试");
            }
            borrowWithLock(dto, userId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "借阅锁获取失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void borrowWithLock(BookBorrowDTO dto, Long userId) {
        Book book = bookMapper.selectById(dto.getBookId());
        if (book == null || book.getDeleted() == 1) {
            throw new BusinessException(404, "图书不存在");
        }
        if (book.getStatus() == 0) {
            throw new BusinessException(409, "该图书已下架，无法借阅");
        }
        if (book.getAvailableCount() == null || book.getAvailableCount() <= 0) {
            throw new BusinessException(409, "该图书已全部借出");
        }

        Long duplicated = bookBorrowMapper.selectCount(new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getBookId, dto.getBookId())
                .eq(BookBorrow::getUserId, userId)
                .eq(BookBorrow::getStatus, "BORROWED")
                .eq(BookBorrow::getDeleted, 0));
        if (duplicated > 0) {
            throw new BusinessException(409, "您已借阅过该图书，请勿重复借阅");
        }

        book.setAvailableCount(book.getAvailableCount() - 1);
        book.setUpdateTime(LocalDateTime.now());
        bookMapper.updateById(book);

        BookBorrow borrow = new BookBorrow();
        borrow.setBookId(dto.getBookId());
        borrow.setUserId(userId);
        borrow.setBorrowTime(LocalDateTime.now());
        borrow.setStatus("BORROWED");
        borrow.setDeleted(0);
        borrow.setCreateTime(LocalDateTime.now());
        borrow.setUpdateTime(LocalDateTime.now());
        bookBorrowMapper.insert(borrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long bookId) {
        Long userId = requireLogin();

        RLock lock = redissonClient.getLock("ca:lock:borrow:" + bookId);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(409, "当前请求较多，请稍后重试");
            }
            returnBookWithLock(bookId, userId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "归还锁获取失败");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void returnBookWithLock(Long bookId, Long userId) {
        BookBorrow borrow = bookBorrowMapper.selectOne(new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getBookId, bookId)
                .eq(BookBorrow::getUserId, userId)
                .eq(BookBorrow::getStatus, "BORROWED")
                .eq(BookBorrow::getDeleted, 0));
        if (borrow == null) {
            throw new BusinessException(409, "未找到该图书的借阅记录，请确认是否已借阅");
        }

        Book book = bookMapper.selectById(bookId);
        if (book != null && book.getDeleted() == 0) {
            book.setAvailableCount((book.getAvailableCount() == null ? 0 : book.getAvailableCount()) + 1);
            book.setUpdateTime(LocalDateTime.now());
            bookMapper.updateById(book);
        }

        borrow.setStatus("RETURNED");
        borrow.setReturnTime(LocalDateTime.now());
        borrow.setUpdateTime(LocalDateTime.now());
        bookBorrowMapper.updateById(borrow);
    }

    @Override
    public List<BookBorrow> myBorrows() {
        Long userId = requireLogin();
        return bookBorrowMapper.selectList(new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getUserId, userId)
                .eq(BookBorrow::getDeleted, 0)
                .orderByDesc(BookBorrow::getCreateTime));
    }

    @Override
    public Page<BookBorrowVO> borrowPage(Long current, Long size, String keyword) {
        RoleUtils.requireAny("ADMIN");
        LambdaQueryWrapper<BookBorrow> wrapper = new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            // 先查询匹配的图书ID，再按图书ID筛选借阅记录
            List<Long> matchedBookIds = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                            .eq(Book::getDeleted, 0)
                            .and(w -> w.like(Book::getTitle, keyword)
                                    .or()
                                    .like(Book::getAuthor, keyword)
                                    .or()
                                    .like(Book::getIsbn, keyword)))
                    .stream().map(Book::getId).distinct().toList();
            if (matchedBookIds.isEmpty()) {
                return Page.of(current, size, 0);
            }
            wrapper.in(BookBorrow::getBookId, matchedBookIds);
        }
        Page<BookBorrow> page = bookBorrowMapper.selectPage(Page.of(current, size),
                wrapper.orderByDesc(BookBorrow::getCreateTime));
        List<Long> bookIds = page.getRecords().stream().map(BookBorrow::getBookId).distinct().toList();
        List<Long> userIds = page.getRecords().stream().map(BookBorrow::getUserId).distinct().toList();
        java.util.Map<Long, Book> bookMap = bookIds.isEmpty() ? java.util.Collections.emptyMap()
                : bookMapper.selectBatchIds(bookIds).stream().collect(java.util.stream.Collectors.toMap(Book::getId, b -> b));
        java.util.Map<Long, User> userMap = userIds.isEmpty() ? java.util.Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream().collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        List<BookBorrowVO> voList = page.getRecords().stream().map(b -> {
            Book book = bookMap.get(b.getBookId());
            User user = userMap.get(b.getUserId());
            return BookBorrowVO.builder()
                    .id(b.getId())
                    .bookId(b.getBookId())
                    .bookTitle(book == null ? "未知图书" : book.getTitle())
                    .bookAuthor(book == null ? "" : book.getAuthor())
                    .userId(b.getUserId())
                    .userRealName(user == null ? "未知用户" : user.getRealName())
                    .userStudentNo(user == null ? "" : user.getStudentNo())
                    .borrowTime(b.getBorrowTime())
                    .returnTime(b.getReturnTime())
                    .status(b.getStatus())
                    .build();
        }).toList();
        Page<BookBorrowVO> voPage = Page.of(current, size, page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
