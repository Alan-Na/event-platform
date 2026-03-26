package com.eventflow.platform.messaging;

import com.eventflow.platform.enums.NotificationType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotificationMessage {
    private NotificationType eventType;
    private Long userId;
    private Long eventId;
    private String templateKey;
    private Map<String, Object> templateParams;
}
