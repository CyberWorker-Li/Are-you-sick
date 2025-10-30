// AuthController.java
package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor


public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 患者注册
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ApiResponse.success("注册成功", null);
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ApiResponse.success("密码重置成功", null);
        } catch (RuntimeException e) {
            log.error("密码重置失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        try {
            authService.sendVerificationCode(request);
            return ApiResponse.success("验证码已发送至邮箱", null);
        } catch (RuntimeException e) {
            log.error("发送验证码失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
