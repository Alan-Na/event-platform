package com.eventflow.platform.dto.admin;

import java.util.Set;

public record AdminUserOverviewDto(
        Long userId,
        String fullName,
        String email,
        Set<String> roles,
        long confirmedBookings,
        long waitingEntries,
        long cancelledRecords) {
}
