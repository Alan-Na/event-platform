package com.eventflow.platform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.mapper.EventMapper;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.TicketTypeRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizerEventServiceTest {

    @Mock private AdminEventService adminEventService;
    @Mock private EventRepository eventRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private WaitlistEntryRepository waitlistEntryRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;

    private OrganizerEventService organizerEventService;

    @BeforeEach
    void setUp() {
        organizerEventService = new OrganizerEventService(
                adminEventService, eventRepository, bookingRepository,
                waitlistEntryRepository, ticketTypeRepository,
                new EventMapper(), new BookingMapper());
    }

    // ── Ownership enforcement ─────────────────────────────────────────────────

    @Test
    void organizerCannotManageAnotherOrganizersEvent() {
        User owner = buildUser(10L, "owner@test.com");
        Event event = buildEvent(1L, owner, EventStatus.DRAFT);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));

        // organizerId 99 != owner 10
        assertThatThrownBy(() -> organizerEventService.requireOwned(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not own");
    }

    @Test
    void organizerCanAccessOwnEvent() {
        User owner = buildUser(10L, "owner@test.com");
        Event event = buildEvent(1L, owner, EventStatus.DRAFT);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));

        Event result = organizerEventService.requireOwned(1L, 10L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void requireOwnedThrowsWhenEventHasNoCreatedBy() {
        Event event = buildEvent(1L, null, EventStatus.DRAFT);

        when(eventRepository.findDetailedById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> organizerEventService.requireOwned(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("do not own");
    }

    // ── publishEvent ownership guard ──────────────────────────────────────────

    @Test
    void organizerCannotPublishUnownedEvent() {
        User owner = buildUser(10L, "owner@test.com");
        Event event = buildEvent(2L, owner, EventStatus.DRAFT);

        when(eventRepository.findDetailedById(2L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> organizerEventService.publishEvent(2L, 99L))
                .isInstanceOf(BusinessException.class);

        verify(adminEventService, never()).publishEvent(anyLong());
    }

    @Test
    void organizerCanPublishOwnEvent() {
        User owner = buildUser(10L, "owner@test.com");
        Event event = buildEvent(2L, owner, EventStatus.DRAFT);

        when(eventRepository.findDetailedById(2L)).thenReturn(Optional.of(event));
        when(adminEventService.publishEvent(2L)).thenReturn(null);

        organizerEventService.publishEvent(2L, 10L);

        verify(adminEventService).publishEvent(2L);
    }

    // ── closeEvent ownership guard ────────────────────────────────────────────

    @Test
    void organizerCannotCloseUnownedEvent() {
        User owner = buildUser(10L, "owner@test.com");
        Event event = buildEvent(3L, owner, EventStatus.PUBLISHED);

        when(eventRepository.findDetailedById(3L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> organizerEventService.closeEvent(3L, 99L))
                .isInstanceOf(BusinessException.class);

        verify(adminEventService, never()).closeEvent(anyLong());
    }

    // ── getMyEvents filters by organizer ─────────────────────────────────────

    @Test
    void getMyEventsReturnsOnlyOwnEvents() {
        User owner = buildUser(10L, "owner@test.com");
        Event e1 = buildEvent(1L, owner, EventStatus.PUBLISHED);
        Event e2 = buildEvent(2L, owner, EventStatus.DRAFT);

        when(eventRepository.findAllByCreatedByIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(e1, e2));

        PageResponse<EventSummaryDto> page = organizerEventService.getMyEvents(10L, null, null, 0, 10);

        assertThat(page.items()).hasSize(2);
    }

    @Test
    void getMyEventsFiltersByStatus() {
        User owner = buildUser(10L, "owner@test.com");
        Event e1 = buildEvent(1L, owner, EventStatus.PUBLISHED);
        Event e2 = buildEvent(2L, owner, EventStatus.DRAFT);

        when(eventRepository.findAllByCreatedByIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(e1, e2));

        PageResponse<EventSummaryDto> page = organizerEventService.getMyEvents(10L, "PUBLISHED", null, 0, 10);

        assertThat(page.items()).hasSize(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User buildUser(Long id, String email) {
        User u = User.builder().email(email).fullName("Test User").enabled(true).build();
        u.setId(id);
        return u;
    }

    private Event buildEvent(Long id, User createdBy, EventStatus status) {
        Event e = Event.builder()
                .slug("test-event-" + id).title("Test Event " + id)
                .summary("summary").description("desc")
                .locationName("Venue").city("NYC")
                .startTime(OffsetDateTime.now().plusDays(10))
                .endTime(OffsetDateTime.now().plusDays(10).plusHours(2))
                .registrationDeadline(OffsetDateTime.now().plusDays(5))
                .capacity(100).confirmedCount(0).waitlistCount(0)
                .featured(false).status(status)
                .createdBy(createdBy).build();
        e.setId(id);
        return e;
    }
}
