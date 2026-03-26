package com.eventflow.platform.controller.admin;

import com.eventflow.platform.dto.admin.AdminBookingItemDto;
import com.eventflow.platform.dto.admin.AdminWaitlistItemDto;
import com.eventflow.platform.dto.admin.CancelEventRequest;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.CreateEventRequest;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.dto.event.UpdateEventRequest;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.AdminEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    public ApiResponse<PageResponse<EventSummaryDto>> getEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", "Admin events fetched", adminEventService.getEvents(status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDetailDto> getEventDetail(@PathVariable Long id) {
        return ApiResponse.success("OK", "Event detail fetched", adminEventService.getEventDetail(id));
    }

    @PostMapping
    public ApiResponse<EventDetailDto> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("CREATED", "Event created", adminEventService.createEvent(request, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<EventDetailDto> updateEvent(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Event updated", adminEventService.updateEvent(id, request, userId));
    }

    @PatchMapping("/{id}/publish")
    public ApiResponse<EventDetailDto> publishEvent(@PathVariable Long id) {
        return ApiResponse.success("OK", "Event published", adminEventService.publishEvent(id));
    }

    @PatchMapping("/{id}/close")
    public ApiResponse<EventDetailDto> closeEvent(@PathVariable Long id) {
        return ApiResponse.success("OK", "Event closed", adminEventService.closeEvent(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<EventDetailDto> cancelEvent(@PathVariable Long id, @Valid @RequestBody(required = false) CancelEventRequest request) {
        String reason = request == null ? null : request.reason();
        return ApiResponse.success("OK", "Event cancelled", adminEventService.cancelEvent(id, reason));
    }

    @GetMapping("/{id}/bookings")
    public ApiResponse<PageResponse<AdminBookingItemDto>> getBookings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", "Booking list fetched", adminEventService.getEventBookings(id, page, size));
    }

    @GetMapping("/{id}/waitlist")
    public ApiResponse<PageResponse<AdminWaitlistItemDto>> getWaitlist(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", "Waitlist fetched", adminEventService.getEventWaitlist(id, page, size));
    }
}
