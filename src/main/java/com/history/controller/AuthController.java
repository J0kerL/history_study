package com.history.controller;

import com.history.common.Result;
import com.history.model.dto.LoginDTO;
import com.history.model.dto.RefreshTokenDTO;
import com.history.model.dto.RegisterDTO;
import com.history.model.dto.SendVerificationCodeDTO;
import com.history.model.vo.LoginVO;
import com.history.model.vo.RegisterVO;
import com.history.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口
 *
 * @author Diamond
 */
@Validated
@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口", description = "认证相关接口")
public class AuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功",loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        RegisterVO registerVO = authService.register(registerDTO);
        return Result.success("注册成功", registerVO);
    }

    @PostMapping("/send-verification-code")
    @Operation(summary = "发送手机验证码", description = "发送手机验证码接口")
    public Result<Map<String, Object>> sendVerificationCode(@Valid @RequestBody SendVerificationCodeDTO request) {
        Map<String, Object> result = authService.sendVerificationCode(request.getPhone());
        return Result.success("验证码发送成功", result);
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "刷新 AccessToken", description = "使用 RefreshToken 刷新 AccessToken")
    public Result<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        Map<String, Object> result = authService.refreshToken(refreshTokenDTO.getRefreshToken());
        return Result.success(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "用户退出登录接口")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
