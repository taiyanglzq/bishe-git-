package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.JwtUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.LoginDTO;
import com.campus.assistant.dto.PasswordUpdateDTO;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.AuthService;
import com.campus.assistant.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getDeleted, 0));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRoleCode());
        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(user.getRoleCode())
                .initialPassword(user.getInitialPassword())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateDTO dto) {
        User current = UserContext.get();
        if (current == null) {
            throw new BusinessException(401, "请先登录");
        }
        User user = userMapper.selectById(current.getId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setInitialPassword(0);
        userMapper.updateById(user);
    }
}
