package com.eventflow.platform.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        CurrentUserDto user) {
}
