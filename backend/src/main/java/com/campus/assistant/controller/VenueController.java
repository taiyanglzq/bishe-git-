package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.VenueSaveDTO;
import com.campus.assistant.entity.Venue;
import com.campus.assistant.service.VenueService;
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

/**
 * 场地控制器，负责接收场地分页和场地管理请求并调用场地服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/venue")
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/page")
    public Result<Page<Venue>> page(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "10") Long size) {
        return Result.success(venueService.page(current, size));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody VenueSaveDTO dto) {
        return Result.success(venueService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody VenueSaveDTO dto) {
        venueService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return Result.success();
    }
}
