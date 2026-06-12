package com.campus.assistant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.assistant.dto.UserSaveDTO;
import com.campus.assistant.vo.UserVO;

/**
 * 用户服务接口，定义当前用户查询和后台用户管理相关业务能力。
 */
public interface UserService {

    UserVO current();

    Page<UserVO> page(Long current, Long size, String keyword);

    Long save(UserSaveDTO dto);

    void update(UserSaveDTO dto);

    void delete(Long id);
}
