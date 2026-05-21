package com.eventflow.platform.dto.organizer;

import java.util.List;

public record OrganizerDashboardDto(
        long totalEvents,
        long publishedEvents,
        long totalConfirmedBookings,
        long totalWaitlistCount,
        long totalCheckedIn,
        List<OrganizerEventStatsDto> upcomingEvents) {
}
