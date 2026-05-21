package com.eventflow.platform.controller;

import com.eventflow.platform.dto.checkin.CheckInRequest;
import com.eventflow.platform.dto.checkin.CheckInResponseDto;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events/{eventId}/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ApiResponse<CheckInResponseDto> checkIn(
            @PathVariable Long eventId,
            @Valid @RequestBody CheckInRequest request,
            Authentication authentication) {
        Long checkInById = SecurityUtils.getCurrentUserId().orElseThrow();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ApiResponse.success("OK", "Check-in successful",
                checkInService.checkIn(eventId, request.confirmationCode(), checkInById, isAdmin));
    }
}
