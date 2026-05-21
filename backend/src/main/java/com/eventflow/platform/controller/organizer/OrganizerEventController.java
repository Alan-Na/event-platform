package com.eventflow.platform.controller.organizer;

import com.eventflow.platform.dto.admin.AdminBookingItemDto;
import com.eventflow.platform.dto.admin.AdminWaitlistItemDto;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.event.CreateEventRequest;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.dto.event.UpdateEventRequest;
import com.eventflow.platform.dto.organizer.OrganizerDashboardDto;
import com.eventflow.platform.dto.organizer.OrganizerEventStatsDto;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.OrganizerEventService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
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
@RequestMapping("/api/v1/organizer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
public class OrganizerEventController {

    private final OrganizerEventService organizerEventService;

    @GetMapping("/dashboard")
    public ApiResponse<OrganizerDashboardDto> getDashboard() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Dashboard fetched", organizerEventService.getDashboard(userId));
    }

    @GetMapping("/events")
    public ApiResponse<PageResponse<EventSummaryDto>> getMyEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "My events fetched",
                organizerEventService.getMyEvents(userId, status, keyword, page, size));
    }

    @PostMapping("/events")
    public ApiResponse<EventDetailDto> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("CREATED", "Event created",
                organizerEventService.createEvent(request, userId));
    }

    @PutMapping("/events/{id}")
    public ApiResponse<EventDetailDto> updateEvent(@PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Event updated",
                organizerEventService.updateEvent(id, request, userId));
    }

    @PatchMapping("/events/{id}/publish")
    public ApiResponse<EventDetailDto> publishEvent(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Event published",
                organizerEventService.publishEvent(id, userId));
    }

    @PatchMapping("/events/{id}/close")
    public ApiResponse<EventDetailDto> closeEvent(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Event closed",
                organizerEventService.closeEvent(id, userId));
    }

    @GetMapping("/events/{id}/stats")
    public ApiResponse<OrganizerEventStatsDto> getEventStats(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Stats fetched",
                organizerEventService.getEventStats(id, userId));
    }

    @GetMapping("/events/{id}/bookings")
    public ApiResponse<PageResponse<AdminBookingItemDto>> getBookings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Bookings fetched",
                organizerEventService.getEventBookings(id, userId, page, size));
    }

    @GetMapping("/events/{id}/waitlist")
    public ApiResponse<PageResponse<AdminWaitlistItemDto>> getWaitlist(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Waitlist fetched",
                organizerEventService.getEventWaitlist(id, userId, page, size));
    }

    @GetMapping("/events/{id}/registrations.csv")
    public void exportCsv(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        organizerEventService.exportRegistrationsCsv(id, userId, response);
    }
}
