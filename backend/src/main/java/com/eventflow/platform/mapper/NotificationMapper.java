package com.eventflow.platform.mapper;

import com.eventflow.platform.dto.notification.NotificationDto;
import com.eventflow.platform.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTemplateKey(),
                notification.getPayloadJson(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
