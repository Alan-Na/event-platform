package com.eventflow.platform.service;

import com.eventflow.platform.dto.admin.AdminBookingItemDto;
import com.eventflow.platform.dto.admin.AdminWaitlistItemDto;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.CreateEventRequest;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.dto.event.UpdateEventRequest;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Category;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.TicketType;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.NotificationType;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.exception.ErrorCode;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.mapper.EventMapper;
import com.eventflow.platform.messaging.BookingNotificationMessage;
import com.eventflow.platform.messaging.NotificationPublisher;
import com.eventflow.platform.metrics.BusinessMetricsService;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.CategoryRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.TicketTypeRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import com.eventflow.platform.util.DateTimeUtil;
import com.eventflow.platform.util.EventSpecifications;
import com.eventflow.platform.util.SlugUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventMapper eventMapper;
    private final BookingMapper bookingMapper;
    private final NotificationPublisher notificationPublisher;
    private final BusinessMetricsService metrics;

    @Transactional(readOnly = true)
    public EventDetailDto getEventDetail(Long eventId) {
        Event event = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        var ticketTypes = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
        return eventMapper.toDetailDto(event, event.getStatus() == EventStatus.PUBLISHED, null, null, ticketTypes);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventSummaryDto> getEvents(String status, String keyword, int page, int size) {
        Specification<Event> specification = Specification.where(EventSpecifications.keywordContains(keyword));
        if (status != null && !status.isBlank()) {
            specification = specification.and(EventSpecifications.statusEquals(EventStatus.valueOf(status.toUpperCase())));
        }
        Page<EventSummaryDto> result = eventRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(eventMapper::toSummaryDto);
        return PageResponse.from(result);
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public EventDetailDto createEvent(CreateEventRequest request, Long adminUserId) {
        validateEventWindow(request.startTime(), request.endTime(), request.registrationDeadline(),
                request.capacity(), 0);
        Category category = categoryRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        String slug = uniqueSlug(request.title());
        Event event = Event.builder()
                .slug(slug).title(request.title()).summary(request.summary())
                .description(request.description()).coverImageUrl(request.coverImageUrl())
                .locationName(request.locationName()).address(request.address()).city(request.city())
                .startTime(request.startTime()).endTime(request.endTime())
                .registrationDeadline(request.registrationDeadline())
                .capacity(request.capacity()).confirmedCount(0).waitlistCount(0)
                .featured(Boolean.TRUE.equals(request.featured()))
                .status(EventStatus.DRAFT).category(category)
                .createdBy(admin).updatedBy(admin).build();
        event.setTags(request.tags() == null ? new java.util.HashSet<>() : new java.util.HashSet<>(request.tags()));
        Event saved = eventRepository.save(event);

        // Auto-create a default General Admission ticket type
        TicketType gaTicket = TicketType.builder()
                .event(saved).name("General Admission")
                .description("Standard event access")
                .priceAmount(BigDecimal.ZERO).currency("USD")
                .capacity(request.capacity()).confirmedCount(0)
                .salesStartAt(DateTimeUtil.nowUtc()).salesEndAt(request.registrationDeadline())
                .active(true).build();
        ticketTypeRepository.save(gaTicket);

        return eventMapper.toDetailDto(saved, false, "EVENT_NOT_PUBLISHED", null, List.of(gaTicket));
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public EventDetailDto updateEvent(Long eventId, UpdateEventRequest request, Long adminUserId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        validateEventWindow(request.startTime(), request.endTime(), request.registrationDeadline(),
                request.capacity(), event.getConfirmedCount());
        Category category = categoryRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        event.setTitle(request.title()); event.setSummary(request.summary());
        event.setDescription(request.description()); event.setCoverImageUrl(request.coverImageUrl());
        event.setLocationName(request.locationName()); event.setAddress(request.address());
        event.setCity(request.city()); event.setStartTime(request.startTime());
        event.setEndTime(request.endTime()); event.setRegistrationDeadline(request.registrationDeadline());
        event.setCapacity(request.capacity()); event.setFeatured(Boolean.TRUE.equals(request.featured()));
        event.setCategory(category); event.setUpdatedBy(admin);
        event.setTags(request.tags() == null ? new java.util.HashSet<>() : new java.util.HashSet<>(request.tags()));
        event.setSlug(uniqueSlugForUpdate(event.getId(), request.title()));

        var ticketTypes = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
        return eventMapper.toDetailDto(event, event.getStatus() == EventStatus.PUBLISHED, null, null, ticketTypes);
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public EventDetailDto publishEvent(Long eventId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        validateEventWindow(event.getStartTime(), event.getEndTime(), event.getRegistrationDeadline(),
                event.getCapacity(), event.getConfirmedCount());
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.EVENT_NOT_BOOKABLE, "Cancelled event cannot be published");
        }
        event.setStatus(EventStatus.PUBLISHED);
        event.setPublishedAt(DateTimeUtil.nowUtc());
        metrics.recordEventStatusTransition("DRAFT", "PUBLISHED");
        var ticketTypes = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
        return eventMapper.toDetailDto(event, true, null, null, ticketTypes);
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public EventDetailDto closeEvent(Long eventId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() != EventStatus.CANCELLED) {
            metrics.recordEventStatusTransition(event.getStatus().name(), "CLOSED");
            event.setStatus(EventStatus.CLOSED);
        }
        var ticketTypes = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
        return eventMapper.toDetailDto(event, false, "EVENT_CLOSED", null, ticketTypes);
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public EventDetailDto cancelEvent(Long eventId, String reason) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.CANCELLED) {
            var tt = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
            return eventMapper.toDetailDto(event, false, "EVENT_CANCELLED", null, tt);
        }
        metrics.recordEventStatusTransition(event.getStatus().name(), "CANCELLED");
        event.setStatus(EventStatus.CANCELLED);
        event.setCancelledAt(DateTimeUtil.nowUtc());

        List<Booking> confirmedBookings = bookingRepository.findAllByEventIdAndStatus(eventId, BookingStatus.CONFIRMED);
        confirmedBookings.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(DateTimeUtil.nowUtc());
            publishEventCancelled(booking.getUser().getId(), event, reason);
        });
        List<WaitlistEntry> waitingEntries = waitlistEntryRepository.findAllByEventIdAndStatus(eventId, WaitlistStatus.WAITING);
        waitingEntries.forEach(entry -> {
            entry.setStatus(WaitlistStatus.EXPIRED);
            entry.setCancelledAt(DateTimeUtil.nowUtc());
            publishEventCancelled(entry.getUser().getId(), event, reason);
        });
        event.setConfirmedCount(0);
        event.setWaitlistCount(0);
        var ticketTypes = ticketTypeRepository.findAllByEventIdAndActiveTrueOrderByIdAsc(eventId);
        return eventMapper.toDetailDto(event, false, "EVENT_CANCELLED", null, ticketTypes);
    }

    public PageResponse<AdminBookingItemDto> getEventBookings(Long eventId, int page, int size) {
        Page<AdminBookingItemDto> result = bookingRepository
                .findAllByEventIdAndStatusOrderByBookedAtAsc(eventId, BookingStatus.CONFIRMED,
                        PageRequest.of(page, size))
                .map(bookingMapper::toAdminBookingItem);
        return PageResponse.from(result);
    }

    public PageResponse<AdminWaitlistItemDto> getEventWaitlist(Long eventId, int page, int size) {
        Page<AdminWaitlistItemDto> result = waitlistEntryRepository
                .findAllByEventIdAndStatusOrderByPositionAsc(eventId, WaitlistStatus.WAITING,
                        PageRequest.of(page, size))
                .map(bookingMapper::toAdminWaitlistItem);
        return PageResponse.from(result);
    }

    /** Streams confirmed bookings as CSV. Caller must ensure the requester is authorized. */
    public void exportRegistrationsCsv(Long eventId, HttpServletResponse response) throws IOException {
        Event event = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"registrations-" + eventId + ".csv\"");

        List<Booking> bookings = bookingRepository.findAllByEventIdAndStatusIn(eventId,
                List.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("booking_id,user_name,user_email,status,ticket_type,booked_at,confirmation_code,checked_in,checked_in_at");
            for (Booking b : bookings) {
                writer.println(String.join(",",
                        str(b.getId()),
                        csvEscape(b.getUser().getFullName()),
                        csvEscape(b.getUser().getEmail()),
                        str(b.getStatus()),
                        csvEscape(b.getTicketType() != null ? b.getTicketType().getName() : ""),
                        b.getBookedAt() != null ? b.getBookedAt().format(fmt) : "",
                        str(b.getConfirmationCode()),
                        b.getCheckedInAt() != null ? "true" : "false",
                        b.getCheckedInAt() != null ? b.getCheckedInAt().format(fmt) : ""));
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String uniqueSlug(String title) {
        String base = SlugUtil.slugify(title);
        return eventRepository.existsBySlug(base) ? base + "-" + System.currentTimeMillis() : base;
    }

    private String uniqueSlugForUpdate(Long eventId, String title) {
        String base = SlugUtil.slugify(title);
        if (!eventRepository.existsBySlug(base)) return base;
        Event existing = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return existing.getSlug().equals(base) ? base : base + "-" + eventId;
    }

    private void validateEventWindow(java.time.OffsetDateTime startTime, java.time.OffsetDateTime endTime,
            java.time.OffsetDateTime registrationDeadline, int capacity, int confirmedCount) {
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "End time must be after start time");
        }
        if (registrationDeadline.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Registration deadline must be before start time");
        }
        if (capacity < confirmedCount) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Capacity cannot be lower than confirmed count");
        }
    }

    private void publishEventCancelled(Long userId, Event event, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventTitle", event.getTitle());
        payload.put("reason", reason == null ? "" : reason);
        metrics.recordNotificationPublished(NotificationType.EVENT_CANCELLED.name());
        notificationPublisher.publishAfterCommit("notification.event.cancelled",
                BookingNotificationMessage.builder()
                        .eventType(NotificationType.EVENT_CANCELLED).userId(userId)
                        .eventId(event.getId()).templateKey("notifications.eventCancelled")
                        .templateParams(payload).build());
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }
}
