package com.eventflow.platform.dto.admin;

import java.time.OffsetDateTime;

public record AdminBookingItemDto(
        Long bookingId,
        Long userId,
        String userName,
        String userEmail,
        String status,
        String source,
        OffsetDateTime bookedAt) {
}
