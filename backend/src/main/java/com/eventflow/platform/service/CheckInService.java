package com.eventflow.platform.service;

import com.eventflow.platform.dto.checkin.CheckInResponseDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.exception.ErrorCode;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.metrics.BusinessMetricsService;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.service.OrganizerEventService;
import com.eventflow.platform.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerEventService organizerEventService;
    private final BusinessMetricsService metrics;

    /**
     * Performs check-in by confirmation code.
     *
     * @param eventId       target event
     * @param confirmationCode supplied by the attendee
     * @param checkInById   user performing the check-in (must be ADMIN or event ORGANIZER)
     * @param isAdmin       true if the caller has ROLE_ADMIN
     */
    @Transactional
    public CheckInResponseDto checkIn(Long eventId, String confirmationCode, Long checkInById, boolean isAdmin) {
        Event event = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Authorization: admin or organizer of this event
        if (!isAdmin) {
            organizerEventService.requireOwned(eventId, checkInById);
        }

        Booking booking = bookingRepository.findByEventIdAndConfirmationCode(eventId, confirmationCode)
                .orElseThrow(() -> {
                    metrics.recordCheckIn("not_found");
                    return new BusinessException(ErrorCode.BOOKING_NOT_FOUND,
                            "No booking found for this confirmation code");
                });

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            metrics.recordCheckIn("cancelled_booking");
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Booking is cancelled");
        }
        if (booking.getCheckedInAt() != null) {
            metrics.recordCheckIn("already_checked_in");
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED, "Already checked in");
        }

        User checkInBy = userRepository.findById(checkInById)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

        booking.setCheckedInAt(DateTimeUtil.nowUtc());
        booking.setCheckedInBy(checkInBy);
        metrics.recordCheckIn("success");

        return new CheckInResponseDto(
                booking.getId(),
                booking.getConfirmationCode(),
                event.getId(),
                event.getTitle(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getTicketType() != null ? booking.getTicketType().getName() : null,
                booking.getCheckedInAt(),
                checkInBy.getFullName());
    }
}
