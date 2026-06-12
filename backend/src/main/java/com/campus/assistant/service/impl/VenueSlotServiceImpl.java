package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.dto.VenueSlotSaveDTO;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.mapper.VenueSlotMapper;
import com.campus.assistant.service.VenueSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场地时间段服务实现，负责时间段库存分页与后台维护业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class VenueSlotServiceImpl implements VenueSlotService {

    private final VenueSlotMapper venueSlotMapper;

    @Override
    public Page<VenueSlot> page(Long current, Long size, Long venueId, LocalDate slotDate) {
        return venueSlotMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getDeleted, 0)
                .eq(venueId != null, VenueSlot::getVenueId, venueId)
                .eq(slotDate != null, VenueSlot::getSlotDate, slotDate)
                .orderByDesc(VenueSlot::getSlotDate)
                .orderByAsc(VenueSlot::getTimeRange));
    }

    @Override
    public Long save(VenueSlotSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        VenueSlot slot = new VenueSlot();
        slot.setVenueId(dto.getVenueId());
        slot.setSlotDate(dto.getSlotDate());
        slot.setTimeRange(dto.getTimeRange());
        slot.setTotalQuota(dto.getTotalQuota());
        slot.setRemainingQuota(dto.getRemainingQuota() == null ? dto.getTotalQuota() : dto.getRemainingQuota());
        slot.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        slot.setDeleted(0);
        slot.setCreateTime(LocalDateTime.now());
        slot.setUpdateTime(LocalDateTime.now());
        venueSlotMapper.insert(slot);
        return slot.getId();
    }

    @Override
    public void update(VenueSlotSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        VenueSlot slot = venueSlotMapper.selectById(dto.getId());
        if (slot == null) {
            throw new BusinessException(404, "预约时间段不存在");
        }
        slot.setVenueId(dto.getVenueId());
        slot.setSlotDate(dto.getSlotDate());
        slot.setTimeRange(dto.getTimeRange());
        slot.setTotalQuota(dto.getTotalQuota());
        slot.setRemainingQuota(dto.getRemainingQuota() == null ? slot.getRemainingQuota() : dto.getRemainingQuota());
        slot.setStatus(dto.getStatus() == null ? slot.getStatus() : dto.getStatus());
        slot.setUpdateTime(LocalDateTime.now());
        venueSlotMapper.updateById(slot);
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        VenueSlot slot = venueSlotMapper.selectById(id);
        if (slot != null) {
            slot.setDeleted(1);
            slot.setUpdateTime(LocalDateTime.now());
            venueSlotMapper.updateById(slot);
        }
    }
}
