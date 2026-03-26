package com.eventflow.platform.messaging;

import com.eventflow.platform.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAfterCommit(String routingKey, BookingNotificationMessage message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, message);
                }
            });
        } else {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, message);
        }
    }
}
