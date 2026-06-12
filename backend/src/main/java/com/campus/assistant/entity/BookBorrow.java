package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书借阅实体，对应 ca_book_borrow 表
 */
@Data
@TableName("ca_book_borrow")
public class BookBorrow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 图书ID */
    private Long bookId;

    /** 借阅用户ID */
    private Long userId;

    /** 借阅时间 */
    private LocalDateTime borrowTime;

    /** 归还时间 */
    private LocalDateTime returnTime;

    /** 借阅状态：BORROWED-借阅中 / RETURNED-已归还 */
    private String status;

    /** 逻辑删除 */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
