package com.history.service;

import com.history.model.dto.LoginDTO;
import com.history.model.dto.RegisterDTO;
import com.history.model.vo.RegisterVO;
import com.history.model.vo.LoginVO;

import java.util.Map;

/**
 * @Author Diamond
 * @Create 2026/3/24
 */
public interface AuthService {
    LoginVO login(LoginDTO loginDTO);

    RegisterVO register(RegisterDTO registerDTO);

    Map<String, Object> sendVerificationCode(String phone);

    Map<String, Object> refreshToken(String refreshToken);

    void logout();
}
