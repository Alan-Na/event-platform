package com.eventflow.platform.dto.auth;

import java.util.Set;

public record CurrentUserDto(
        Long id,
        String email,
        String fullName,
        String preferredLanguage,
        Set<String> roles,
        boolean enabled) {
}
