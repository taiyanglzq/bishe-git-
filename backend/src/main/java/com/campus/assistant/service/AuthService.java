package com.campus.assistant.service;

import com.campus.assistant.dto.LoginDTO;
import com.campus.assistant.dto.PasswordUpdateDTO;
import com.campus.assistant.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void updatePassword(PasswordUpdateDTO dto);
}
