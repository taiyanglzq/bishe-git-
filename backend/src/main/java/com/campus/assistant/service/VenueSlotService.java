package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.VenueSlotSaveDTO;
import com.campus.assistant.entity.VenueSlot;

import java.time.LocalDate;

/**
 * 场地时间段服务接口，定义时间段库存分页查询和后台维护相关业务能力。
 */
public interface VenueSlotService {

    Page<VenueSlot> page(Long current, Long size, Long venueId, LocalDate slotDate);

    Long save(VenueSlotSaveDTO dto);

    void update(VenueSlotSaveDTO dto);

    void delete(Long id);
}
