package com.eventflow.platform.controller.admin;

import com.eventflow.platform.dto.admin.AdminUserOverviewDto;
import com.eventflow.platform.dto.booking.MyBookingItemDto;
import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<AdminUserOverviewDto>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", "Users fetched", adminUserService.getUsers(keyword, page, size));
    }

    @GetMapping("/{id}/bookings")
    public ApiResponse<PageResponse<MyBookingItemDto>> getUserBookings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success("OK", "User bookings fetched", adminUserService.getUserBookings(id, page, size));
    }
}
