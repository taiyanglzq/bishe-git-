package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.VenueSlotSaveDTO;
import com.campus.assistant.entity.VenueSlot;
import com.campus.assistant.service.VenueSlotService;
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

import java.time.LocalDate;

/**
 * 场地时间段控制器，负责接收时间段库存分页和维护请求并调用时间段服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/venue-slot")
public class VenueSlotController {

    private final VenueSlotService venueSlotService;

    @GetMapping("/page")
    public Result<Page<VenueSlot>> page(@RequestParam(defaultValue = "1") Long current,
                                        @RequestParam(defaultValue = "10") Long size,
                                        @RequestParam(required = false) Long venueId,
                                        @RequestParam(required = false) LocalDate slotDate) {
        return Result.success(venueSlotService.page(current, size, venueId, slotDate));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody VenueSlotSaveDTO dto) {
        return Result.success(venueSlotService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody VenueSlotSaveDTO dto) {
        venueSlotService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        venueSlotService.delete(id);
        return Result.success();
    }
}
