package com.eventflow.platform.controller;

import com.eventflow.platform.dto.booking.BookingActionResponseDto;
import com.eventflow.platform.dto.booking.MyBookingItemDto;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/events/{eventId}/bookings")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<BookingActionResponseDto> bookEvent(@PathVariable Long eventId) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Booking processed", bookingService.bookEvent(userId, eventId));
    }

    @DeleteMapping("/events/{eventId}/bookings/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<BookingActionResponseDto> cancelBooking(@PathVariable Long eventId) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Booking cancelled", bookingService.cancelRegistration(userId, eventId));
    }

    @GetMapping("/me/bookings")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<PageResponse<MyBookingItemDto>> getMyBookings(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "My bookings fetched", bookingService.getMyBookings(userId, status, page, size));
    }
}
