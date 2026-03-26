package com.eventflow.platform.dto.booking;

public record BookingActionResponseDto(
        Long eventId,
        String registrationState,
        Long bookingId,
        Long waitlistEntryId,
        Integer waitlistPosition,
        Integer remainingSeats,
        Integer confirmedCount,
        Integer waitlistCount,
        String messageKey) {
}
