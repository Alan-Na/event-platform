package com.eventflow.platform.dto.notification;

import com.eventflow.platform.enums.NotificationType;
import java.time.OffsetDateTime;

public record NotificationDto(
        Long id,
        NotificationType type,
        String templateKey,
        String payloadJson,
        boolean isRead,
        OffsetDateTime createdAt) {
}
