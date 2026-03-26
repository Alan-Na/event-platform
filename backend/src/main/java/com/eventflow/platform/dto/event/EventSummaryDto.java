package com.eventflow.platform.dto.event;

import com.eventflow.platform.enums.EventStatus;
import java.time.OffsetDateTime;
import java.util.Set;

public record EventSummaryDto(
        Long id,
        String slug,
        String title,
        String summary,
        String coverImageUrl,
        String city,
        String locationName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        OffsetDateTime registrationDeadline,
        Integer capacity,
        Integer confirmedCount,
        Integer remainingSeats,
        Boolean featured,
        EventStatus status,
        String categoryCode,
        Set<String> tags) {
}
