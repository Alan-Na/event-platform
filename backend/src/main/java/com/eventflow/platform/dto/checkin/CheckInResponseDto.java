package com.eventflow.platform.dto.checkin;

import java.time.OffsetDateTime;

public record CheckInResponseDto(
        Long bookingId,
        String confirmationCode,
        Long eventId,
        String eventTitle,
        String userDisplayName,
        String userEmail,
        String ticketTypeName,
        OffsetDateTime checkedInAt,
        String checkedInByName) {
}
