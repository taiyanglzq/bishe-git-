package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.dto.VenueSaveDTO;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.mapper.VenueMapper;
import com.campus.assistant.mapper.VenueSlotMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ?? ??????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/venue")
public class VenueController {

    private final VenueMapper venueMapper;
    private final VenueSlotMapper venueSlotMapper;

    @GetMapping("/page")
    public Result<Page<Venue>> page(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "10") Long size) {
        return Result.success(venueMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<Venue>()
                .eq(Venue::getDeleted, 0)
                .orderByDesc(Venue::getCreateTime)));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody VenueSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setImageUrl(dto.getImageUrl());
        venue.setCapacity(dto.getCapacity());
        venue.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        venue.setDeleted(0);
        venue.setCreateTime(LocalDateTime.now());
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.insert(venue);
        return Result.success(venue.getId());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody VenueSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = venueMapper.selectById(dto.getId());
        if (venue == null) {
            return Result.fail(404, "场地不存在");
        }
        Long oversizedSlots = venueSlotMapper.selectCount(new LambdaQueryWrapper<VenueSlot>()
                .eq(VenueSlot::getVenueId, venue.getId())
                .eq(VenueSlot::getDeleted, 0)
                .gt(VenueSlot::getTotalQuota, dto.getCapacity()));
        if (oversizedSlots > 0) {
            return Result.fail(409, "已有开放时间段名额超过新容量，请先调整时间段库存");
        }
        venue.setName(dto.getName());
        venue.setLocation(dto.getLocation());
        venue.setImageUrl(dto.getImageUrl());
        venue.setCapacity(dto.getCapacity());
        venue.setStatus(dto.getStatus() == null ? venue.getStatus() : dto.getStatus());
        venue.setUpdateTime(LocalDateTime.now());
        venueMapper.updateById(venue);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RoleUtils.requireAny("ADMIN");
        Venue venue = venueMapper.selectById(id);
        if (venue != null) {
            venue.setDeleted(1);
            venue.setUpdateTime(LocalDateTime.now());
            venueMapper.updateById(venue);
        }
        return Result.success();
    }
}
