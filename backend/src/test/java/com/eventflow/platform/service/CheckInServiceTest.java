package com.eventflow.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventflow.platform.dto.checkin.CheckInResponseDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.BookingSource;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.metrics.BusinessMetricsService;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrganizerEventService organizerEventService;

    private CheckInService checkInService;

    @BeforeEach
    void setUp() {
        BusinessMetricsService metrics = new BusinessMetricsService(new SimpleMeterRegistry());
        checkInService = new CheckInService(
                bookingRepository, eventRepository, userRepository,
                organizerEventService, metrics);
    }

    // ── Admin can check in any event ──────────────────────────────────────────

    @Test
    void adminCanCheckInAnyEvent() {
        Event event = buildEvent(1L, 10L);
        User attendee = buildUser(200L, "attendee@test.com");
        User admin = buildUser(1L, "admin@test.com");
        Booking booking = buildConfirmedBooking(event, attendee, "EFTEST1234567");

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndConfirmationCode(1L, "EFTEST1234567")).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        CheckInResponseDto response = checkInService.checkIn(1L, "EFTEST1234567", 1L, true);

        assertThat(response.confirmationCode()).isEqualTo("EFTEST1234567");
        assertThat(response.checkedInAt()).isNotNull();
        assertThat(booking.getCheckedInAt()).isNotNull();
        assertThat(booking.getCheckedInBy()).isEqualTo(admin);
        // Admin bypasses ownership check
        verify(organizerEventService, never()).requireOwned(anyLong(), anyLong());
    }

    // ── Organizer can check in own event ──────────────────────────────────────

    @Test
    void organizerCanCheckInOwnEvent() {
        Event event = buildEvent(1L, 10L);
        User attendee = buildUser(200L, "attendee@test.com");
        User organizer = buildUser(10L, "org@test.com");
        Booking booking = buildConfirmedBooking(event, attendee, "EFORG1234567A");

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndConfirmationCode(1L, "EFORG1234567A")).thenReturn(Optional.of(booking));
        when(userRepository.findById(10L)).thenReturn(Optional.of(organizer));

        CheckInResponseDto response = checkInService.checkIn(1L, "EFORG1234567A", 10L, false);

        assertThat(response.checkedInAt()).isNotNull();
        verify(organizerEventService).requireOwned(1L, 10L);
    }

    // ── Organizer cannot check in another's event ─────────────────────────────

    @Test
    void organizerCannotCheckInOtherOrganizersEvent() {
        Event event = buildEvent(1L, 10L);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(organizerEventService.requireOwned(1L, 99L))
                .thenThrow(new BusinessException(
                        com.eventflow.platform.exception.ErrorCode.FORBIDDEN,
                        "You do not own this event"));

        assertThatThrownBy(() -> checkInService.checkIn(1L, "EFANY1234567A", 99L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not own");

        verify(bookingRepository, never()).findByEventIdAndConfirmationCode(any(), any());
    }

    // ── Duplicate check-in rejected ───────────────────────────────────────────

    @Test
    void duplicateCheckInIsRejected() {
        Event event = buildEvent(1L, 10L);
        User attendee = buildUser(200L, "attendee@test.com");
        User admin = buildUser(1L, "admin@test.com");
        Booking booking = buildConfirmedBooking(event, attendee, "EFDUP12345678");
        booking.setCheckedInAt(OffsetDateTime.now().minusMinutes(5));
        booking.setCheckedInBy(admin);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndConfirmationCode(1L, "EFDUP12345678")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> checkInService.checkIn(1L, "EFDUP12345678", 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Already checked in");
    }

    // ── Cancelled booking cannot be checked in ────────────────────────────────

    @Test
    void cancelledBookingCannotBeCheckedIn() {
        Event event = buildEvent(1L, 10L);
        User attendee = buildUser(200L, "attendee@test.com");
        Booking booking = buildConfirmedBooking(event, attendee, "EFCAN12345678");
        booking.setStatus(BookingStatus.CANCELLED);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndConfirmationCode(1L, "EFCAN12345678")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> checkInService.checkIn(1L, "EFCAN12345678", 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelled");
    }

    // ── Booking not found ─────────────────────────────────────────────────────

    @Test
    void unknownConfirmationCodeThrows() {
        Event event = buildEvent(1L, 10L);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEventIdAndConfirmationCode(1L, "EFNOTEXIST000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.checkIn(1L, "EFNOTEXIST000", 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No booking found");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Event buildEvent(Long id, Long createdById) {
        User createdBy = buildUser(createdById, "organizer@test.com");
        Event e = Event.builder()
                .slug("test-event-" + id).title("Test Event")
                .summary("summary").description("desc")
                .locationName("Venue").city("NYC")
                .startTime(OffsetDateTime.now().plusDays(10))
                .endTime(OffsetDateTime.now().plusDays(10).plusHours(2))
                .registrationDeadline(OffsetDateTime.now().plusDays(5))
                .capacity(100).confirmedCount(10).waitlistCount(0)
                .featured(false).status(EventStatus.PUBLISHED)
                .createdBy(createdBy).build();
        e.setId(id);
        return e;
    }

    private User buildUser(Long id, String email) {
        User u = User.builder().email(email).fullName("Test User").enabled(true).build();
        u.setId(id);
        return u;
    }

    private Booking buildConfirmedBooking(Event event, User user, String code) {
        Booking b = Booking.builder()
                .event(event).user(user).status(BookingStatus.CONFIRMED)
                .source(BookingSource.DIRECT).bookedAt(OffsetDateTime.now())
                .confirmationCode(code).build();
        b.setId(50L);
        return b;
    }
}
