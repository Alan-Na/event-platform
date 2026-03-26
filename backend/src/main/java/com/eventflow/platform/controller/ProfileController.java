package com.eventflow.platform.controller;

import com.eventflow.platform.dto.auth.CurrentUserDto;
import com.eventflow.platform.dto.auth.UpdateProfileRequest;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<CurrentUserDto> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Profile fetched", profileService.getProfile(userId));
    }

    @PatchMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<CurrentUserDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Profile updated", profileService.updateProfile(userId, request));
    }
}
