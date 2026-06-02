package com.campus.assistant.controller;

import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.LoginDTO;
import com.campus.assistant.dto.PasswordUpdateDTO;
import com.campus.assistant.service.AuthService;
import com.campus.assistant.vo.LoginVO;
import com.campus.assistant.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/current")
    public Result<UserVO> current() {
        return Result.success(UserVO.from(UserContext.get()));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        authService.updatePassword(dto);
        return Result.success();
    }
}
