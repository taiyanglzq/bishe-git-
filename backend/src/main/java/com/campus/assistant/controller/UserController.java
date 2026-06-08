package com.campus.assistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.result.Result;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.PasswordUpdateDTO;
import com.campus.assistant.dto.UserSaveDTO;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.AuthService;
import com.campus.assistant.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * ?? ??????????????????????
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/current")
    public Result<UserVO> current() {
        return Result.success(UserVO.from(UserContext.get()));
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
        RoleUtils.requireAny("ADMIN");
        Page<User> page = userMapper.selectPage(Page.of(current, size), new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword))
                .orderByDesc(User::getCreateTime));
        Page<UserVO> voPage = Page.of(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(UserVO::from).toList());
        return Result.success(voPage);
    }

    @PostMapping
    public Result<Long> save(@Valid @RequestBody UserSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        String loginNo = resolveLoginNo(dto);
        User user = new User();
        user.setUsername(loginNo);
        user.setRealName(dto.getRealName());
        user.setStudentNo("STUDENT".equals(dto.getRoleCode()) ? loginNo : null);
        user.setCollege(dto.getCollege());
        user.setRoleCode(dto.getRoleCode());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setDeleted(0);
        user.setInitialPassword(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        String rawPassword = dto.getPassword() == null || dto.getPassword().isBlank() ? "123456" : dto.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.insert(user);
        return Result.success(user.getId());
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        String loginNo = resolveLoginNo(dto);
        user.setUsername(loginNo);
        user.setRealName(dto.getRealName());
        user.setStudentNo("STUDENT".equals(dto.getRoleCode()) ? loginNo : null);
        user.setCollege(dto.getCollege());
        user.setRoleCode(dto.getRoleCode());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setInitialPassword(1);
        }
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        RoleUtils.requireAny("ADMIN");
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setDeleted(1);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return Result.success();
    }

    private String resolveLoginNo(UserSaveDTO dto) {
        String loginNo = dto.getLoginNo();
        if (loginNo == null || loginNo.isBlank()) {
            loginNo = dto.getUsername();
        }
        return loginNo == null ? "" : loginNo.trim();
    }
}
