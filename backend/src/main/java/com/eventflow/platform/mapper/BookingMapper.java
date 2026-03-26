package com.eventflow.platform.mapper;

import com.eventflow.platform.dto.admin.AdminBookingItemDto;
import com.eventflow.platform.dto.admin.AdminWaitlistItemDto;
import com.eventflow.platform.dto.booking.MyBookingItemDto;
import com.eventflow.platform.entity.Booking;
import com.eventflow.platform.entity.WaitlistEntry;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public MyBookingItemDto toMyBookingItem(Booking booking) {
        return new MyBookingItemDto(
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getEvent().getCoverImageUrl(),
                booking.getEvent().getCity(),
                booking.getEvent().getLocationName(),
                booking.getEvent().getStartTime(),
                booking.getEvent().getEndTime(),
                "BOOKING",
                booking.getStatus().name(),
                booking.getBookedAt(),
                null,
                booking.getCancelledAt(),
                null,
                booking.getStatus().name().equals("CONFIRMED"));
    }

    public MyBookingItemDto toMyBookingItem(WaitlistEntry waitlistEntry) {
        return new MyBookingItemDto(
                waitlistEntry.getEvent().getId(),
                waitlistEntry.getEvent().getTitle(),
                waitlistEntry.getEvent().getCoverImageUrl(),
                waitlistEntry.getEvent().getCity(),
                waitlistEntry.getEvent().getLocationName(),
                waitlistEntry.getEvent().getStartTime(),
                waitlistEntry.getEvent().getEndTime(),
                "WAITLIST",
                waitlistEntry.getStatus().name(),
                null,
                waitlistEntry.getJoinedAt(),
                waitlistEntry.getCancelledAt(),
                waitlistEntry.getPosition(),
                waitlistEntry.getStatus().name().equals("WAITING"));
    }

    public AdminBookingItemDto toAdminBookingItem(Booking booking) {
        return new AdminBookingItemDto(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getStatus().name(),
                booking.getSource().name(),
                booking.getBookedAt());
    }

    public AdminWaitlistItemDto toAdminWaitlistItem(WaitlistEntry waitlistEntry) {
        return new AdminWaitlistItemDto(
                waitlistEntry.getId(),
                waitlistEntry.getUser().getId(),
                waitlistEntry.getUser().getFullName(),
                waitlistEntry.getUser().getEmail(),
                waitlistEntry.getPosition(),
                waitlistEntry.getStatus().name(),
                waitlistEntry.getJoinedAt(),
                waitlistEntry.getPromotedAt());
    }
}
