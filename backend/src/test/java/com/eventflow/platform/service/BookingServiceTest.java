package com.eventflow.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventflow.platform.dto.booking.BookingActionResponseDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.TicketType;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.BookingSource;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.messaging.NotificationPublisher;
import com.eventflow.platform.metrics.BusinessMetricsService;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.TicketTypeRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private WaitlistEntryRepository waitlistEntryRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private NotificationPublisher notificationPublisher;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        BusinessMetricsService metrics = new BusinessMetricsService(new SimpleMeterRegistry());
        bookingService = new BookingService(
                eventRepository, userRepository, bookingRepository,
                waitlistEntryRepository, ticketTypeRepository,
                new BookingMapper(), notificationPublisher, metrics);
    }

    // ── Booking success ───────────────────────────────────────────────────────

    @Test
    void shouldBookSuccessfullyWhenSeatsAvailable() {
        Event event = buildEvent(2, 0, 0);
        User user = buildUser(100L, "alice@test.com");
        TicketType ticket = buildTicket(event, 2, 0);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findFirstByEventIdAndActiveTrueOrderByIdAsc(1L)).thenReturn(Optional.of(ticket));
        when(ticketTypeRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, WaitlistStatus.WAITING)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookingActionResponseDto response = bookingService.bookEvent(100L, 1L, null);

        assertThat(response.registrationState()).isEqualTo("CONFIRMED");
        assertThat(response.bookingId()).isEqualTo(10L);
        assertThat(response.confirmationCode()).isNotNull().startsWith("EF");
        assertThat(event.getConfirmedCount()).isEqualTo(1);
        assertThat(ticket.getConfirmedCount()).isEqualTo(1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void confirmedBookingMustHaveConfirmationCode() {
        Event event = buildEvent(2, 0, 0);
        User user = buildUser(100L, "alice@test.com");
        TicketType ticket = buildTicket(event, 2, 0);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findFirstByEventIdAndActiveTrueOrderByIdAsc(1L)).thenReturn(Optional.of(ticket));
        when(ticketTypeRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(ticket));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(11L);
            return b;
        });

        BookingActionResponseDto response = bookingService.bookEvent(100L, 1L, null);

        assertThat(response.confirmationCode()).isNotBlank();
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    @Test
    void shouldReturnExistingBookingForDuplicateRegistration() {
        Event event = buildEvent(2, 1, 0);
        User user = buildUser(100L, "alice@test.com");
        TicketType ticket = buildTicket(event, 2, 1);
        Booking existing = buildConfirmedBooking(event, user, "EF1234567890");

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findFirstByEventIdAndActiveTrueOrderByIdAsc(1L)).thenReturn(Optional.of(ticket));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.of(existing));

        BookingActionResponseDto response = bookingService.bookEvent(100L, 1L, null);

        assertThat(response.registrationState()).isEqualTo("CONFIRMED");
        assertThat(response.bookingId()).isEqualTo(11L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ── Waitlist ──────────────────────────────────────────────────────────────

    @Test
    void shouldJoinWaitlistWhenTicketTypeFull() {
        Event event = buildEvent(1, 1, 0);
        User user = buildUser(101L, "bob@test.com");
        TicketType ticket = buildTicket(event, 1, 1); // full

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findFirstByEventIdAndActiveTrueOrderByIdAsc(1L)).thenReturn(Optional.of(ticket));
        when(ticketTypeRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 101L, BookingStatus.CONFIRMED)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(1L, 101L, WaitlistStatus.WAITING)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionByEventId(1L)).thenReturn(0);
        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenAnswer(inv -> {
            WaitlistEntry w = inv.getArgument(0);
            w.setId(20L);
            return w;
        });

        BookingActionResponseDto response = bookingService.bookEvent(101L, 1L, null);

        assertThat(response.registrationState()).isEqualTo("WAITING");
        assertThat(response.waitlistPosition()).isEqualTo(1);
        assertThat(event.getWaitlistCount()).isEqualTo(1);
    }

    // ── Cancellation + waitlist promotion ─────────────────────────────────────

    @Test
    void shouldPromoteWaitlistWhenConfirmedBookingCancels() {
        Event event = buildEvent(1, 1, 1);
        User originalUser = buildUser(100L, "alice@test.com");
        User nextUser = buildUser(101L, "bob@test.com");
        TicketType ticket = buildTicket(event, 1, 1);

        Booking confirmed = buildConfirmedBooking(event, originalUser, "EFORIGINAL01");
        confirmed.setTicketType(ticket);

        WaitlistEntry waiting = WaitlistEntry.builder()
                .event(event).user(nextUser).ticketType(ticket)
                .position(1).status(WaitlistStatus.WAITING).joinedAt(OffsetDateTime.now()).build();
        waiting.setId(40L);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.of(confirmed));
        when(waitlistEntryRepository.findFirstByEventIdAndTicketTypeIdAndStatusOrderByPositionAsc(1L, ticket.getId(), WaitlistStatus.WAITING))
                .thenReturn(Optional.of(waiting));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        BookingActionResponseDto response = bookingService.cancelRegistration(100L, 1L);

        assertThat(response.registrationState()).isEqualTo("CANCELLED");
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(waiting.getStatus()).isEqualTo(WaitlistStatus.PROMOTED);
        assertThat(ticket.getConfirmedCount()).isEqualTo(1); // decremented then incremented for promoted
        assertThat(event.getWaitlistCount()).isEqualTo(0);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldNotBookCancelledEvent() {
        Event event = buildEvent(10, 0, 0);
        event.setStatus(EventStatus.CANCELLED);
        User user = buildUser(100L, "alice@test.com");

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> bookingService.bookEvent(100L, 1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not open for booking");
    }

    @Test
    void cancelledBookingTicketCountDecreases() {
        Event event = buildEvent(1, 1, 0);
        User user = buildUser(100L, "alice@test.com");
        TicketType ticket = buildTicket(event, 1, 1);
        Booking confirmed = buildConfirmedBooking(event, user, "EFCANCEL12345");
        confirmed.setTicketType(ticket);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.of(confirmed));
        when(waitlistEntryRepository.findFirstByEventIdAndTicketTypeIdAndStatusOrderByPositionAsc(any(), any(), any())).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndStatusOrderByPositionAsc(any(), any())).thenReturn(Optional.empty());

        bookingService.cancelRegistration(100L, 1L);

        assertThat(ticket.getConfirmedCount()).isEqualTo(0);
        assertThat(event.getConfirmedCount()).isEqualTo(0);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Event buildEvent(int capacity, int confirmedCount, int waitlistCount) {
        Event event = Event.builder()
                .title("Test Event").capacity(capacity)
                .confirmedCount(confirmedCount).waitlistCount(waitlistCount)
                .status(EventStatus.PUBLISHED)
                .startTime(OffsetDateTime.now().plusDays(5))
                .endTime(OffsetDateTime.now().plusDays(5).plusHours(2))
                .registrationDeadline(OffsetDateTime.now().plusDays(1))
                .build();
        event.setId(1L);
        return event;
    }

    private User buildUser(Long id, String email) {
        User user = User.builder().email(email).fullName("Test User").enabled(true).build();
        user.setId(id);
        return user;
    }

    private TicketType buildTicket(Event event, int capacity, int confirmed) {
        TicketType t = TicketType.builder()
                .event(event).name("General Admission")
                .priceAmount(BigDecimal.ZERO).currency("USD")
                .capacity(capacity).confirmedCount(confirmed)
                .active(true).build();
        t.setId(10L);
        return t;
    }

    private Booking buildConfirmedBooking(Event event, User user, String code) {
        Booking b = Booking.builder()
                .event(event).user(user).status(BookingStatus.CONFIRMED)
                .source(BookingSource.DIRECT).bookedAt(OffsetDateTime.now())
                .confirmationCode(code).build();
        b.setId(11L);
        return b;
    }
}
