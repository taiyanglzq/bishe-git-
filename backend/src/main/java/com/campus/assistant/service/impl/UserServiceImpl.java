package com.campus.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.common.utils.RoleUtils;
import com.campus.assistant.common.utils.UserContext;
import com.campus.assistant.dto.UserSaveDTO;
import com.campus.assistant.entity.User;
import com.campus.assistant.mapper.UserMapper;
import com.campus.assistant.service.UserService;
import com.campus.assistant.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现，负责当前用户查询和后台用户管理相关业务逻辑。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO current() {
        return UserVO.from(UserContext.get());
    }

    @Override
    public Page<UserVO> page(Long current, Long size, String keyword, String roleCode) {
        RoleUtils.requireAny("ADMIN");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0);
        if (roleCode != null && !roleCode.isBlank()) {
            wrapper.eq(User::getRoleCode, roleCode);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(Page.of(current, size), wrapper);
        Page<UserVO> voPage = Page.of(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(UserVO::from).toList());
        return voPage;
    }

    @Override
    public Long save(UserSaveDTO dto) {
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
        return user.getId();
    }

    @Override
    public void update(UserSaveDTO dto) {
        RoleUtils.requireAny("ADMIN");
        User user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
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
    }

    @Override
    public void delete(Long id) {
        RoleUtils.requireAny("ADMIN");
        User user = userMapper.selectById(id);
        if (user != null) {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, id)
                    .set(User::getDeleted, 1)
                    .set(User::getUpdateTime, LocalDateTime.now()));
        }
    }

    private String resolveLoginNo(UserSaveDTO dto) {
        String loginNo = dto.getLoginNo();  //从 DTO 中取出前端传来的 loginNo（学号/工号/自定义登录账号）
        if (loginNo == null || loginNo.isBlank()) {
            loginNo = dto.getUsername();
        }
        //如果解析后还是空字符串，直接抛出异常
        if (loginNo.isBlank()) {
            throw new BusinessException(400, "登录账号不能为空");
        }
        return loginNo.trim();  //去掉首尾空格
        /*因为 null 不能调用 .trim() 方法（会抛出空指针异常）。所以要先判断*/
    }
}
