package com.eventflow.platform.dto.booking;

import java.time.OffsetDateTime;

public record MyBookingItemDto(
        Long eventId,
        String eventTitle,
        String coverImageUrl,
        String city,
        String locationName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String recordType,
        String status,
        OffsetDateTime bookedAt,
        OffsetDateTime joinedAt,
        OffsetDateTime cancelledAt,
        Integer waitlistPosition,
        boolean canCancel) {
}
