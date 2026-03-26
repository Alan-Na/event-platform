package com.eventflow.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventflow.platform.dto.booking.BookingActionResponseDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.BookingSource;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.messaging.NotificationPublisher;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
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

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private WaitlistEntryRepository waitlistEntryRepository;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                eventRepository,
                userRepository,
                bookingRepository,
                waitlistEntryRepository,
                new BookingMapper(),
                notificationPublisher);
    }

    @Test
    void shouldBookSuccessfullyWhenSeatsAvailable() {
        Event event = buildEvent(2, 0, 0);
        User user = buildUser(100L, "alice@test.com");

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, WaitlistStatus.WAITING)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });

        BookingActionResponseDto response = bookingService.bookEvent(100L, 1L);

        assertThat(response.registrationState()).isEqualTo("CONFIRMED");
        assertThat(response.bookingId()).isEqualTo(10L);
        assertThat(event.getConfirmedCount()).isEqualTo(1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldReturnExistingBookingForDuplicateRegistration() {
        Event event = buildEvent(2, 1, 0);
        User user = buildUser(100L, "alice@test.com");
        Booking existing = Booking.builder()
                .event(event)
                .user(user)
                .status(BookingStatus.CONFIRMED)
                .source(BookingSource.DIRECT)
                .bookedAt(OffsetDateTime.now())
                .build();
        existing.setId(11L);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.of(existing));

        BookingActionResponseDto response = bookingService.bookEvent(100L, 1L);

        assertThat(response.registrationState()).isEqualTo("CONFIRMED");
        assertThat(response.bookingId()).isEqualTo(11L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldJoinWaitlistWhenEventIsFull() {
        Event event = buildEvent(1, 1, 0);
        User user = buildUser(101L, "bob@test.com");

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 101L, BookingStatus.CONFIRMED)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(1L, 101L, WaitlistStatus.WAITING)).thenReturn(Optional.empty());
        when(waitlistEntryRepository.findMaxPositionByEventId(1L)).thenReturn(0);
        when(waitlistEntryRepository.save(any(WaitlistEntry.class))).thenAnswer(invocation -> {
            WaitlistEntry entry = invocation.getArgument(0);
            entry.setId(20L);
            return entry;
        });

        BookingActionResponseDto response = bookingService.bookEvent(101L, 1L);

        assertThat(response.registrationState()).isEqualTo("WAITING");
        assertThat(response.waitlistPosition()).isEqualTo(1);
        assertThat(event.getWaitlistCount()).isEqualTo(1);
    }

    @Test
    void shouldPromoteWaitlistWhenConfirmedBookingCancels() {
        Event event = buildEvent(1, 1, 1);
        User originalUser = buildUser(100L, "alice@test.com");
        User nextUser = buildUser(101L, "bob@test.com");
        Booking confirmedBooking = Booking.builder()
                .event(event)
                .user(originalUser)
                .status(BookingStatus.CONFIRMED)
                .source(BookingSource.DIRECT)
                .bookedAt(OffsetDateTime.now())
                .build();
        confirmedBooking.setId(30L);
        WaitlistEntry waitlistEntry = WaitlistEntry.builder()
                .event(event)
                .user(nextUser)
                .position(1)
                .status(WaitlistStatus.WAITING)
                .joinedAt(OffsetDateTime.now())
                .build();
        waitlistEntry.setId(40L);

        when(eventRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.findFirstByEventIdAndUserIdAndStatus(1L, 100L, BookingStatus.CONFIRMED)).thenReturn(Optional.of(confirmedBooking));
        when(waitlistEntryRepository.findFirstByEventIdAndStatusOrderByPositionAsc(1L, WaitlistStatus.WAITING)).thenReturn(Optional.of(waitlistEntry));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(99L);
            return booking;
        });

        BookingActionResponseDto response = bookingService.cancelRegistration(100L, 1L);

        assertThat(response.registrationState()).isEqualTo("CANCELLED");
        assertThat(confirmedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(waitlistEntry.getStatus()).isEqualTo(WaitlistStatus.PROMOTED);
        assertThat(event.getConfirmedCount()).isEqualTo(1);
        assertThat(event.getWaitlistCount()).isEqualTo(0);
        verify(bookingRepository).save(any(Booking.class));
    }

    private Event buildEvent(int capacity, int confirmedCount, int waitlistCount) {
        Event event = Event.builder()
                .title("Test Event")
                .capacity(capacity)
                .confirmedCount(confirmedCount)
                .waitlistCount(waitlistCount)
                .status(EventStatus.PUBLISHED)
                .registrationDeadline(OffsetDateTime.now().plusDays(1))
                .build();
        event.setId(1L);
        return event;
    }

    private User buildUser(Long id, String email) {
        User user = User.builder()
                .email(email)
                .fullName("Test User")
                .enabled(true)
                .build();
        user.setId(id);
        return user;
    }
}
