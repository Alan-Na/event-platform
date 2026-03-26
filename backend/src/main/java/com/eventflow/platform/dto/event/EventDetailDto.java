package com.eventflow.platform.dto.event;

import com.eventflow.platform.enums.EventStatus;
import java.time.OffsetDateTime;
import java.util.Set;

public record EventDetailDto(
        Long id,
        String slug,
        String title,
        String summary,
        String description,
        String coverImageUrl,
        String city,
        String locationName,
        String address,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        OffsetDateTime registrationDeadline,
        Integer capacity,
        Integer confirmedCount,
        Integer remainingSeats,
        Integer waitlistCount,
        Boolean featured,
        EventStatus status,
        String categoryCode,
        Set<String> tags,
        boolean bookable,
        String bookableReason,
        CurrentUserRegistrationDto currentUserRegistration) {
}
