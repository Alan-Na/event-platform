package com.eventflow.platform.controller;

import com.eventflow.platform.dto.auth.AuthResponse;
import com.eventflow.platform.dto.auth.CurrentUserDto;
import com.eventflow.platform.dto.auth.LoginRequest;
import com.eventflow.platform.dto.auth.RegisterRequest;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("REGISTERED", "Registration successful", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("OK", "Login successful", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDto> me() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Current user fetched", authService.getCurrentUser(userId));
    }
}
