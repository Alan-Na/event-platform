package com.eventflow.platform.dto.admin;

import java.time.OffsetDateTime;

public record AdminWaitlistItemDto(
        Long waitlistEntryId,
        Long userId,
        String userName,
        String userEmail,
        Integer position,
        String status,
        OffsetDateTime joinedAt,
        OffsetDateTime promotedAt) {
}
