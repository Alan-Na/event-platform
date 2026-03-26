package com.eventflow.platform.service;

import com.eventflow.platform.dto.common.PageResponse;
import com.eventflow.platform.dto.notification.NotificationDto;
import com.eventflow.platform.entity.Notification;
import com.eventflow.platform.exception.ResourceNotFoundException;
import com.eventflow.platform.mapper.NotificationMapper;
import com.eventflow.platform.repository.NotificationRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public PageResponse<NotificationDto> getNotifications(Long userId, int page, int size, boolean unreadOnly) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> result = (unreadOnly
                ? notificationRepository.findAllByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable)
                : notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable))
                .map(notificationMapper::toDto);
        return PageResponse.from(result);
    }

    @Transactional
    public NotificationDto markRead(Long userId, Long id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notification.setReadAt(OffsetDateTime.now());
        return notificationMapper.toDto(notification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent()
                .forEach(notification -> {
                    notification.setRead(true);
                    notification.setReadAt(OffsetDateTime.now());
                });
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
}
