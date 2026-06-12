package com.campus.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.dto.PasswordUpdateDTO;
import com.campus.assistant.dto.UserSaveDTO;
import com.campus.assistant.service.AuthService;
import com.campus.assistant.service.UserService;
import com.campus.assistant.vo.UserVO;
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
 * 用户控制器，负责接收用户相关请求并调用认证服务或用户服务处理。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/current")
    public Result<UserVO> current() {
        return Result.success(userService.current());
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        authService.updatePassword(dto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<Page<UserVO>> page(@RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size,
                                     @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(current, size, keyword));
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody UserSaveDTO dto) {
        return Result.success(userService.save(dto));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserSaveDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
