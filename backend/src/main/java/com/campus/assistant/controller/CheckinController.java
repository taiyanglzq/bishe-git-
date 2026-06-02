package com.campus.assistant.controller;

import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.CheckinDTO;
import com.campus.assistant.service.ActivityBizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/checkin")
public class CheckinController {

    private final ActivityBizService activityBizService;

    @PostMapping
    public Result<Void> checkin(@Valid @RequestBody CheckinDTO dto) {
        activityBizService.checkin(dto);
        return Result.success();
    }
}
