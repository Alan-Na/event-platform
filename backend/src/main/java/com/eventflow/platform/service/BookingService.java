package com.eventflow.platform.service;

import com.eventflow.platform.dto.booking.BookingActionResponseDto;
import com.eventflow.platform.dto.booking.MyBookingItemDto;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.TicketType;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.entity.WaitlistEntry;
import com.eventflow.platform.enums.BookingSource;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.NotificationType;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.exception.BusinessException;
import com.eventflow.platform.exception.ErrorCode;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.BookingMapper;
import com.eventflow.platform.messaging.BookingNotificationMessage;
import com.eventflow.platform.messaging.NotificationPublisher;
import com.eventflow.platform.metrics.BusinessMetricsService;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.TicketTypeRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import com.eventflow.platform.util.ConfirmationCodeUtil;
import com.eventflow.platform.util.DateTimeUtil;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final BookingMapper bookingMapper;
    private final NotificationPublisher notificationPublisher;
    private final BusinessMetricsService metrics;

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public BookingActionResponseDto bookEvent(Long userId, Long eventId, Long ticketTypeId) {
        // Lock Event first, then TicketType (consistent lock ordering prevents deadlocks)
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateBookableEvent(event);

        // Resolve ticket type — use provided or fall back to the first active one
        TicketType ticketType = resolveTicketType(event, ticketTypeId);

        // Idempotency: already confirmed for this ticket type
        Booking existingBooking = bookingRepository
                .findFirstByEventIdAndUserIdAndStatus(eventId, userId, BookingStatus.CONFIRMED)
                .orElse(null);
        if (existingBooking != null) {
            metrics.recordBookingAttempt("already_confirmed", "direct");
            return buildConfirmedResponse(event, existingBooking.getId(),
                    existingBooking.getConfirmationCode(), "notifications.bookingConfirmed");
        }

        // Idempotency: already on waitlist
        WaitlistEntry existingWaitlist = waitlistEntryRepository
                .findFirstByEventIdAndUserIdAndStatus(eventId, userId, WaitlistStatus.WAITING)
                .orElse(null);
        if (existingWaitlist != null) {
            metrics.recordBookingAttempt("already_waiting", "direct");
            return buildWaitlistResponse(event, existingWaitlist, "notifications.waitlistJoined");
        }

        // Check ticket type capacity (lock it now)
        TicketType lockedTicket = ticketTypeRepository.findByIdForUpdate(ticketType.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        if (lockedTicket.getConfirmedCount() < lockedTicket.getCapacity()) {
            Booking booking = createConfirmedBooking(event, user, lockedTicket, BookingSource.DIRECT, null);
            metrics.recordBookingAttempt("confirmed", "direct");
            publishNotification("notification.booking.confirmed", NotificationType.BOOKING_CONFIRMED,
                    userId, event, Map.of("eventTitle", event.getTitle(),
                            "startTime", event.getStartTime().toString()));
            return buildConfirmedResponse(event, booking.getId(), booking.getConfirmationCode(),
                    "notifications.bookingConfirmed");
        }

        // Full — join waitlist
        Integer maxPosition = waitlistEntryRepository.findMaxPositionByEventId(eventId);
        WaitlistEntry waitlistEntry = WaitlistEntry.builder()
                .event(event)
                .user(user)
                .ticketType(lockedTicket)
                .position((maxPosition == null ? 0 : maxPosition) + 1)
                .status(WaitlistStatus.WAITING)
                .joinedAt(DateTimeUtil.nowUtc())
                .build();
        waitlistEntryRepository.save(waitlistEntry);
        event.setWaitlistCount(event.getWaitlistCount() + 1);
        metrics.recordBookingAttempt("waitlisted", "direct");
        metrics.recordWaitlistJoin();
        publishNotification("notification.booking.waitlisted", NotificationType.WAITLIST_JOINED,
                userId, event, Map.of("eventTitle", event.getTitle(),
                        "position", waitlistEntry.getPosition()));
        return buildWaitlistResponse(event, waitlistEntry, "notifications.waitlistJoined");
    }

    @Transactional
    @CacheEvict(value = "featuredEvents", allEntries = true)
    public BookingActionResponseDto cancelRegistration(Long userId, Long eventId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Booking booking = bookingRepository
                .findFirstByEventIdAndUserIdAndStatus(eventId, userId, BookingStatus.CONFIRMED)
                .orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(DateTimeUtil.nowUtc());
            event.setConfirmedCount(Math.max(0, event.getConfirmedCount() - 1));

            TicketType ticketType = booking.getTicketType();
            if (ticketType != null) {
                ticketType.setConfirmedCount(Math.max(0, ticketType.getConfirmedCount() - 1));
            }

            metrics.recordBookingCancellation();
            publishNotification("notification.booking.cancelled", NotificationType.BOOKING_CANCELLED,
                    userId, event, Map.of("eventTitle", event.getTitle()));

            // Promote first waiting entry (for the same ticket type if possible)
            WaitlistEntry nextWaitlist = resolveNextWaitlistEntry(eventId, ticketType);
            if (nextWaitlist != null) {
                nextWaitlist.setStatus(WaitlistStatus.PROMOTED);
                nextWaitlist.setPromotedAt(DateTimeUtil.nowUtc());
                event.setWaitlistCount(Math.max(0, event.getWaitlistCount() - 1));
                TicketType promotionTicketType = nextWaitlist.getTicketType() != null
                        ? nextWaitlist.getTicketType() : ticketType;
                Booking promotedBooking = createConfirmedBooking(event, nextWaitlist.getUser(),
                        promotionTicketType, BookingSource.WAITLIST_PROMOTION, nextWaitlist);
                metrics.recordWaitlistPromotion();
                publishNotification("notification.booking.promoted", NotificationType.WAITLIST_PROMOTED,
                        nextWaitlist.getUser().getId(), event, Map.of(
                                "eventTitle", event.getTitle(),
                                "bookingId", promotedBooking.getId()));
            }
            return new BookingActionResponseDto(eventId, "CANCELLED", booking.getId(),
                    null, null,
                    event.getCapacity() - event.getConfirmedCount(),
                    event.getConfirmedCount(), event.getWaitlistCount(),
                    "notifications.bookingCancelled", null);
        }

        WaitlistEntry waitlistEntry = waitlistEntryRepository
                .findFirstByEventIdAndUserIdAndStatus(eventId, userId, WaitlistStatus.WAITING)
                .orElse(null);
        if (waitlistEntry != null) {
            waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
            waitlistEntry.setCancelledAt(DateTimeUtil.nowUtc());
            event.setWaitlistCount(Math.max(0, event.getWaitlistCount() - 1));
            return new BookingActionResponseDto(eventId, "CANCELLED", null,
                    waitlistEntry.getId(), waitlistEntry.getPosition(),
                    event.getCapacity() - event.getConfirmedCount(),
                    event.getConfirmedCount(), event.getWaitlistCount(),
                    "notifications.waitlistCancelled", null);
        }

        return new BookingActionResponseDto(eventId, "NONE", null, null, null,
                event.getCapacity() - event.getConfirmedCount(),
                event.getConfirmedCount(), event.getWaitlistCount(),
                "bookings.noActiveRegistration", null);
    }

    public PageResponse<MyBookingItemDto> getMyBookings(Long userId, String status, int page, int size) {
        List<MyBookingItemDto> items = new ArrayList<>();
        bookingRepository.findAllByUserIdOrderByBookedAtDesc(userId).stream()
                .filter(b -> matchesBookingFilter(b, status))
                .map(bookingMapper::toMyBookingItem)
                .forEach(items::add);
        waitlistEntryRepository.findAllByUserIdOrderByJoinedAtDesc(userId).stream()
                .filter(w -> matchesWaitlistFilter(w, status))
                .map(bookingMapper::toMyBookingItem)
                .forEach(items::add);
        items.sort(Comparator.comparing((MyBookingItemDto item) -> {
            OffsetDateTime time = item.bookedAt() != null ? item.bookedAt() : item.joinedAt();
            return time != null ? time : OffsetDateTime.MIN;
        }).reversed());
        int fromIndex = Math.min(page * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        return PageResponse.of(items.subList(fromIndex, toIndex), page, size, items.size());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TicketType resolveTicketType(Event event, Long ticketTypeId) {
        if (ticketTypeId != null) {
            return ticketTypeRepository.findById(ticketTypeId)
                    .filter(t -> t.getEvent().getId().equals(event.getId()) && Boolean.TRUE.equals(t.getActive()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "Ticket type not found or not active for this event"));
        }
        return ticketTypeRepository.findFirstByEventIdAndActiveTrueOrderByIdAsc(event.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "No active ticket type available for this event"));
    }

    private WaitlistEntry resolveNextWaitlistEntry(Long eventId, TicketType preferredTicketType) {
        // Prefer same ticket type, fall back to any WAITING entry
        if (preferredTicketType != null) {
            WaitlistEntry sameType = waitlistEntryRepository
                    .findFirstByEventIdAndTicketTypeIdAndStatusOrderByPositionAsc(
                            eventId, preferredTicketType.getId(), WaitlistStatus.WAITING)
                    .orElse(null);
            if (sameType != null) {
                return sameType;
            }
        }
        return waitlistEntryRepository
                .findFirstByEventIdAndStatusOrderByPositionAsc(eventId, WaitlistStatus.WAITING)
                .orElse(null);
    }

    private void validateBookableEvent(Event event) {
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.EVENT_NOT_BOOKABLE, "Event is not open for booking");
        }
        if (event.getRegistrationDeadline().isBefore(DateTimeUtil.nowUtc())) {
            throw new BusinessException(ErrorCode.EVENT_NOT_BOOKABLE, "Registration deadline has passed");
        }
    }

    private Booking createConfirmedBooking(Event event, User user, TicketType ticketType,
            BookingSource source, WaitlistEntry waitlistEntry) {
        String code = ConfirmationCodeUtil.generate();
        Booking booking = Booking.builder()
                .event(event)
                .user(user)
                .ticketType(ticketType)
                .status(BookingStatus.CONFIRMED)
                .source(source)
                .bookedAt(DateTimeUtil.nowUtc())
                .waitlistEntry(waitlistEntry)
                .confirmationCode(code)
                .build();
        Booking saved = bookingRepository.save(booking);
        event.setConfirmedCount(event.getConfirmedCount() + 1);
        if (ticketType != null) {
            ticketType.setConfirmedCount(ticketType.getConfirmedCount() + 1);
        }
        return saved;
    }

    private BookingActionResponseDto buildConfirmedResponse(Event event, Long bookingId,
            String confirmationCode, String messageKey) {
        return new BookingActionResponseDto(event.getId(), "CONFIRMED", bookingId,
                null, null,
                event.getCapacity() - event.getConfirmedCount(),
                event.getConfirmedCount(), event.getWaitlistCount(),
                messageKey, confirmationCode);
    }

    private BookingActionResponseDto buildWaitlistResponse(Event event, WaitlistEntry entry,
            String messageKey) {
        return new BookingActionResponseDto(event.getId(), "WAITING", null,
                entry.getId(), entry.getPosition(),
                event.getCapacity() - event.getConfirmedCount(),
                event.getConfirmedCount(), event.getWaitlistCount(),
                messageKey, null);
    }

    private void publishNotification(String routingKey, NotificationType type, Long userId,
            Event event, Map<String, Object> params) {
        Map<String, Object> payload = new HashMap<>(params);
        payload.put("eventId", event.getId());
        metrics.recordNotificationPublished(type.name());
        notificationPublisher.publishAfterCommit(routingKey, BookingNotificationMessage.builder()
                .eventType(type)
                .userId(userId)
                .eventId(event.getId())
                .templateKey(switch (type) {
                    case BOOKING_CONFIRMED -> "notifications.bookingConfirmed";
                    case WAITLIST_JOINED -> "notifications.waitlistJoined";
                    case WAITLIST_PROMOTED -> "notifications.waitlistPromoted";
                    case BOOKING_CANCELLED -> "notifications.bookingCancelled";
                    case EVENT_CANCELLED -> "notifications.eventCancelled";
                })
                .templateParams(payload)
                .build());
    }

    private boolean matchesBookingFilter(Booking booking, String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) return true;
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> booking.getStatus() == BookingStatus.CONFIRMED;
            case "CANCELLED" -> booking.getStatus() == BookingStatus.CANCELLED;
            default -> false;
        };
    }

    private boolean matchesWaitlistFilter(WaitlistEntry w, String status) {
        if (w.getStatus() == WaitlistStatus.PROMOTED) return false;
        if (status == null || status.equalsIgnoreCase("ALL")) return true;
        return switch (status.toUpperCase()) {
            case "WAITING" -> w.getStatus() == WaitlistStatus.WAITING;
            case "CANCELLED" -> w.getStatus() == WaitlistStatus.CANCELLED
                    || w.getStatus() == WaitlistStatus.EXPIRED;
            default -> false;
        };
    }
}
