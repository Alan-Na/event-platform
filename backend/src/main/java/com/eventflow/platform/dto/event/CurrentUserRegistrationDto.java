package com.eventflow.platform.dto.event;

public record CurrentUserRegistrationDto(
        String state,
        Long bookingId,
        Long waitlistEntryId,
        Integer waitlistPosition) {
}
