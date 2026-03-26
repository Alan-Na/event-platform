package com.eventflow.platform.service;

import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.CurrentUserRegistrationDto;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventQueryRequest;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.EventMapper;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import com.eventflow.platform.util.DateTimeUtil;
import com.eventflow.platform.util.EventSpecifications;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final EventMapper eventMapper;

    public PageResponse<EventSummaryDto> getPublicEvents(EventQueryRequest queryRequest) {
        Pageable pageable = PageRequest.of(queryRequest.getPage(), queryRequest.getSize(), resolveSort(queryRequest.getSort()));
        Specification<Event> specification = Specification.where(EventSpecifications.statusIn(List.of(EventStatus.PUBLISHED, EventStatus.CLOSED, EventStatus.CANCELLED)))
                .and(EventSpecifications.keywordContains(queryRequest.getKeyword()))
                .and(EventSpecifications.categoryCodeEquals(queryRequest.getCategory()))
                .and(EventSpecifications.cityEquals(queryRequest.getCity()))
                .and(EventSpecifications.startDateFrom(queryRequest.getStartDateFrom()))
                .and(EventSpecifications.startDateTo(queryRequest.getStartDateTo()));

        Page<EventSummaryDto> result = eventRepository.findAll(specification, pageable).map(eventMapper::toSummaryDto);
        return PageResponse.from(result);
    }

    @Cacheable(value = "featuredEvents", key = "#limit")
    public List<EventSummaryDto> getFeaturedEvents(int limit) {
        return eventRepository.findFeaturedPublished(DateTimeUtil.nowUtc(), PageRequest.of(0, limit)).stream()
                .map(eventMapper::toSummaryDto)
                .toList();
    }

    public EventDetailDto getEventDetail(Long eventId, Long currentUserId) {
        Event event = eventRepository.findDetailedById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (event.getStatus() == EventStatus.DRAFT) {
            throw new ResourceNotFoundException("Event not found");
        }

        CurrentUserRegistrationDto registration = currentUserId == null ? null : findCurrentRegistration(currentUserId, eventId);
        String reason = determineBookableReason(event, registration);
        boolean bookable = reason == null;
        return eventMapper.toDetailDto(event, bookable, reason, registration);
    }

    private CurrentUserRegistrationDto findCurrentRegistration(Long userId, Long eventId) {
        Booking booking = bookingRepository.findFirstByEventIdAndUserIdAndStatus(eventId, userId, BookingStatus.CONFIRMED).orElse(null);
        if (booking != null) {
            return new CurrentUserRegistrationDto("CONFIRMED", booking.getId(), null, null);
        }
        WaitlistEntry waitlistEntry = waitlistEntryRepository.findFirstByEventIdAndUserIdAndStatus(eventId, userId, WaitlistStatus.WAITING)
                .orElse(null);
        if (waitlistEntry != null) {
            return new CurrentUserRegistrationDto("WAITING", null, waitlistEntry.getId(), waitlistEntry.getPosition());
        }
        return null;
    }

    private String determineBookableReason(Event event, CurrentUserRegistrationDto registration) {
        OffsetDateTime now = DateTimeUtil.nowUtc();
        if (event.getStatus() == EventStatus.CANCELLED) {
            return "EVENT_CANCELLED";
        }
        if (event.getStatus() == EventStatus.CLOSED) {
            return "EVENT_CLOSED";
        }
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return "EVENT_NOT_PUBLISHED";
        }
        if (event.getRegistrationDeadline().isBefore(now)) {
            return "REGISTRATION_CLOSED";
        }
        if (registration != null) {
            if ("CONFIRMED".equals(registration.state())) {
                return "ALREADY_REGISTERED";
            }
            if ("WAITING".equals(registration.state())) {
                return "ALREADY_WAITING";
            }
        }
        return null;
    }

    private Sort resolveSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.ASC, "startTime");
        }
        return switch (sort) {
            case "START_DESC" -> Sort.by(Sort.Direction.DESC, "startTime");
            case "NEWEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "POPULAR" -> Sort.by(Sort.Direction.DESC, "confirmedCount").and(Sort.by(Sort.Direction.ASC, "startTime"));
            default -> Sort.by(Sort.Direction.ASC, "startTime");
        };
    }
}
