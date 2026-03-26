package com.eventflow.platform.controller;

import com.eventflow.platform.dto.common.ApiResponse;
import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.notification.NotificationDto;
import com.eventflow.platform.security.SecurityUtils;
import com.eventflow.platform.service.NotificationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<PageResponse<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Notifications fetched", notificationService.getNotifications(userId, page, size, unreadOnly));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<Map<String, Long>> unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Unread count fetched", Map.of("unreadCount", notificationService.unreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<NotificationDto> markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        return ApiResponse.success("OK", "Notification marked as read", notificationService.markRead(userId, id));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ApiResponse<Void> markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId().orElseThrow();
        notificationService.markAllRead(userId);
        return ApiResponse.success("OK", "All notifications marked as read", null);
    }
}
