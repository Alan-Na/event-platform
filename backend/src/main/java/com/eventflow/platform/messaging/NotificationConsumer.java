package com.eventflow.platform.messaging;

import com.eventflow.platform.config.RabbitConfig;
import com.eventflow.platform.entity.Notification;
import com.eventflow.platform.entity.User;
import com.eventflow.platform.repository.NotificationRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.util.JsonUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JsonUtil jsonUtil;

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void consume(BookingNotificationMessage message) {
        User user = userRepository.findById(message.getUserId()).orElse(null);
        if (user == null) {
            return;
        }
        Map<String, Object> templateParams = message.getTemplateParams() == null ? Map.of() : message.getTemplateParams();
        Notification notification = Notification.builder()
                .user(user)
                .type(message.getEventType())
                .templateKey(message.getTemplateKey())
                .payloadJson(jsonUtil.toJson(templateParams))
                .read(false)
                .build();
        notificationRepository.save(notification);
    }
}
