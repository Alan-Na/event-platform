package com.eventflow.platform.service;

import com.eventflow.platform.dto.admin.DashboardStatsDto;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.enums.BookingStatus;
import com.eventflow.platform.enums.EventStatus;
import com.eventflow.platform.enums.WaitlistStatus;
import com.eventflow.platform.repository.BookingRepository;
import com.eventflow.platform.repository.EventRepository;
import com.eventflow.platform.repository.UserRepository;
import com.eventflow.platform.repository.WaitlistEntryRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;

    public DashboardStatsDto getDashboardStats() {
        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.countByStatus(EventStatus.PUBLISHED);
        long totalUsers = userRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long waitingEntries = waitlistEntryRepository.countByStatus(WaitlistStatus.WAITING);
        long upcomingEvents = eventRepository.countByStatusAndStartTimeAfter(EventStatus.PUBLISHED, OffsetDateTime.now());
        double averageFillRate = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.PUBLISHED || event.getStatus() == EventStatus.CLOSED)
                .mapToDouble(event -> event.getCapacity() == 0 ? 0D : ((double) event.getConfirmedCount() / event.getCapacity()) * 100)
                .average()
                .orElse(0D);
        return new DashboardStatsDto(totalEvents, publishedEvents, totalUsers, confirmedBookings, waitingEntries, upcomingEvents, Math.round(averageFillRate * 10.0) / 10.0);
    }
}
