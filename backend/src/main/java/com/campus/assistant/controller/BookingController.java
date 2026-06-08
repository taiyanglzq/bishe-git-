package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.AuditDTO;
import com.campus.assistant.dto.BookingCreateDTO;
import com.campus.assistant.entity.Booking;
import com.campus.assistant.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ???? ????????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/page")
    public Result<Page<Booking>> page(@RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size,
                                      @RequestParam(required = false) String status) {
        return Result.success(bookingService.page(current, size, status));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.success(bookingService.create(dto));
    }

    @PutMapping("/approve")
    public Result<Void> approve(@Valid @RequestBody AuditDTO dto) {
        bookingService.approve(dto);
        return Result.success();
    }

    @PutMapping("/reject")
    public Result<Void> reject(@Valid @RequestBody AuditDTO dto) {
        bookingService.reject(dto);
        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return Result.success();
    }
}
