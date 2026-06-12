package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.NoticeCommentCreateDTO;
import com.campus.assistant.dto.NoticeSaveDTO;
import com.campus.assistant.entity.Notice;
import com.campus.assistant.vo.NoticeCommentVO;
import com.campus.assistant.vo.NoticeDetailVO;

import java.util.List;

/**
 * 公告服务接口，定义公告查询、评论和后台管理相关业务能力。
 */
public interface NoticeService {

    Page<Notice> page(Long current, Long size);

    NoticeDetailVO detail(Long id);

    List<NoticeCommentVO> comments(Long id);

    Long comment(NoticeCommentCreateDTO dto);

    Page<Notice> managePage(Long current, Long size);

    Long save(NoticeSaveDTO dto);

    void update(NoticeSaveDTO dto);

    void delete(Long id);

    void approve(Long id);

    void reject(Long id);
}
