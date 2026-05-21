package com.eventflow.platform.service;

import com.eventflow.platform.dto.admin.AdminBookingItemDto;
import com.eventflow.platform.dto.admin.AdminWaitlistItemDto;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.CreateEventRequest;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.dto.event.UpdateEventRequest;
import com.eventflow.platform.dto.organizer.OrganizerDashboardDto;
import com.eventflow.platform.dto.organizer.OrganizerEventStatsDto;
import com.eventflow.platform.dto.ticket.TicketTypeSummaryDto;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.TicketType;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.exception.ErrorCode;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.mapper.EventMapper;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.TicketTypeRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizerEventService {

    private final AdminEventService adminEventService;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventMapper eventMapper;
    private final BookingMapper bookingMapper;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public OrganizerDashboardDto getDashboard(Long organizerId) {
        List<Event> myEvents = eventRepository.findAllByCreatedByIdOrderByCreatedAtDesc(organizerId);

        long totalEvents = myEvents.size();
        long publishedEvents = myEvents.stream().filter(e -> e.getStatus() == EventStatus.PUBLISHED).count();
        long totalConfirmed = myEvents.stream().mapToLong(Event::getConfirmedCount).sum();
        long totalWaitlist = myEvents.stream().mapToLong(Event::getWaitlistCount).sum();
        long totalCheckedIn = myEvents.stream()
                .mapToLong(e -> bookingRepository.countCheckedInByEventId(e.getId())).sum();

        OffsetDateTime now = OffsetDateTime.now();
        List<OrganizerEventStatsDto> upcoming = myEvents.stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED && e.getStartTime().isAfter(now))
                .limit(5)
                .map(e -> toEventStats(e, false))
                .toList();

        return new OrganizerDashboardDto(totalEvents, publishedEvents, totalConfirmed, totalWaitlist, totalCheckedIn, upcoming);
    }

    @Transactional(readOnly = true)
    public OrganizerEventStatsDto getEventStats(Long eventId, Long organizerId) {
        Event event = requireOwned(eventId, organizerId);
        return toEventStats(event, true);
    }

    // ── Event CRUD ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<EventSummaryDto> getMyEvents(Long organizerId, String status, String keyword, int page, int size) {
        List<Event> all = eventRepository.findAllByCreatedByIdOrderByCreatedAtDesc(organizerId);
        List<EventSummaryDto> filtered = all.stream()
                .filter(e -> status == null || status.isBlank() || e.getStatus().name().equalsIgnoreCase(status))
                .filter(e -> keyword == null || keyword.isBlank()
                        || e.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || e.getSummary().toLowerCase().contains(keyword.toLowerCase()))
                .map(eventMapper::toSummaryDto)
                .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return PageResponse.of(filtered.subList(from, to), page, size, filtered.size());
    }

    public EventDetailDto createEvent(CreateEventRequest request, Long organizerId) {
        return adminEventService.createEvent(request, organizerId);
    }

    public EventDetailDto updateEvent(Long eventId, UpdateEventRequest request, Long organizerId) {
        requireOwned(eventId, organizerId);
        return adminEventService.updateEvent(eventId, request, organizerId);
    }

    public EventDetailDto publishEvent(Long eventId, Long organizerId) {
        requireOwned(eventId, organizerId);
        return adminEventService.publishEvent(eventId);
    }

    public EventDetailDto closeEvent(Long eventId, Long organizerId) {
        requireOwned(eventId, organizerId);
        return adminEventService.closeEvent(eventId);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminBookingItemDto> getEventBookings(Long eventId, Long organizerId, int page, int size) {
        requireOwned(eventId, organizerId);
        return adminEventService.getEventBookings(eventId, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminWaitlistItemDto> getEventWaitlist(Long eventId, Long organizerId, int page, int size) {
        requireOwned(eventId, organizerId);
        return adminEventService.getEventWaitlist(eventId, page, size);
    }

    public void exportRegistrationsCsv(Long eventId, Long organizerId, HttpServletResponse response) throws IOException {
        requireOwned(eventId, organizerId);
        adminEventService.exportRegistrationsCsv(eventId, response);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Verifies that the given event exists and belongs to this organizer.
     * Throws FORBIDDEN if ownership check fails.
     */
    public Event requireOwned(Long eventId, Long organizerId) {
        Event event = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getCreatedBy() == null || !event.getCreatedBy().getId().equals(organizerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "You do not own this event");
        }
        return event;
    }

    private OrganizerEventStatsDto toEventStats(Event event, boolean withTicketTypes) {
        long checkedIn = bookingRepository.countCheckedInByEventId(event.getId());
        List<TicketTypeSummaryDto> ticketTypeDtos = withTicketTypes
                ? ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(event.getId())
                        .stream().map(eventMapper::toTicketTypeSummary).toList()
                : List.of();
        return new OrganizerEventStatsDto(event.getId(), event.getTitle(), event.getStatus().name(),
                event.getCapacity(), event.getConfirmedCount(), event.getWaitlistCount(),
                checkedIn, event.getStartTime(), ticketTypeDtos);
    }
}
