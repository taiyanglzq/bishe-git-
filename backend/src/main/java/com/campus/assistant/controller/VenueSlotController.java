package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.dto.VenueSlotSaveDTO;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.mapper.VenueSlotMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ????? ?????????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/venue-slot")
public class VenueSlotController {

    private final VenueSlotMapper venueSlotMapper;

    @GetMapping("/page")
    public Result<Page<VenueSlot>> page(@RequestParam(defaultValue = "1") Long current,
                                        @RequestParam(defaultValue = "10") Long size,
                                        @RequestParam(required = false) Long venueId,
                                        @RequestParam(required = false) LocalDate slotDate) {
        return Result.success(venueSlotMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getDeleted, 0)
                .eq(venueId != null, VenueSlot::getVenueId, venueId)
                .eq(slotDate != null, VenueSlot::getSlotDate, slotDate)
                .orderByDesc(VenueSlot::getSlotDate)
                .orderByAsc(VenueSlot::getTimeRange)));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody VenueSlotSaveDTO dto) {
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
        return Result.success(slot.getId());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody VenueSlotSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        VenueSlot slot = venueSlotMapper.selectById(dto.getId());
        if (slot == null) {
            return Result.fail(404, "预约时间段不存在");
        }
        slot.setVenueId(dto.getVenueId());
        slot.setSlotDate(dto.getSlotDate());
        slot.setTimeRange(dto.getTimeRange());
        slot.setTotalQuota(dto.getTotalQuota());
        slot.setRemainingQuota(dto.getRemainingQuota() == null ? slot.getRemainingQuota() : dto.getRemainingQuota());
        slot.setStatus(dto.getStatus() == null ? slot.getStatus() : dto.getStatus());
        slot.setUpdateTime(LocalDateTime.now());
        venueSlotMapper.updateById(slot);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RoleUtils.requireAny("ADMIN");
        VenueSlot slot = venueSlotMapper.selectById(id);
        if (slot != null) {
            slot.setDeleted(1);
            slot.setUpdateTime(LocalDateTime.now());
            venueSlotMapper.updateById(slot);
        }
        return Result.success();
    }
}
