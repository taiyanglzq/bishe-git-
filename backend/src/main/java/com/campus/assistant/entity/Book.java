package com.campus.assistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书实体，对应 ca_book 表
 */
@Data
@TableName("ca_book")
public class Book {

    /**
     * 图书ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * ISBN号
     */
    private String isbn;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 出版年份
     */
    private String publishYear;

    /**
     * 分类
     */
    private String category;

    /**
     * 馆藏位置
     */
    private String location;

    /**
     * 总册数
     */
    private Integer totalCount;

    /**
     * 可借册数
     */
    private Integer availableCount;

    /**
     * 图书简介
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 状态：0-下架，1-在架
     */
    private Integer status;

    /**
     * 逻辑删除标记
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
