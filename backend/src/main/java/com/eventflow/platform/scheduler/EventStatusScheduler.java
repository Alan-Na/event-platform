package com.eventflow.platform.scheduler;

import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.repository.EventRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "${app.scheduling.close-expired-events-cron}")
    @Transactional
    public void closeExpiredEvents() {
        int updated = eventRepository.closeExpiredPublishedEvents(EventStatus.PUBLISHED, EventStatus.CLOSED, OffsetDateTime.now());
        if (updated > 0) {
            log.info("Closed {} expired event(s)", updated);
        }
    }
}
