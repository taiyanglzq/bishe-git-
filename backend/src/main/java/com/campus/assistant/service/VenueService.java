package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.VenueSaveDTO;
import com.campus.assistant.entity.Venue;

/**
 * 场地服务接口，定义场地分页查询和场地管理相关业务能力。
 */
public interface VenueService {

    Page<Venue> page(Long current, Long size);

    Long save(VenueSaveDTO dto);

    void update(VenueSaveDTO dto);

    void delete(Long id);
}
