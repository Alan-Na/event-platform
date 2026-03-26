package com.eventflow.platform.dto.admin;

public record DashboardStatsDto(
        long totalEvents,
        long publishedEvents,
        long totalUsers,
        long confirmedBookings,
        long waitingEntries,
        long upcomingEvents,
        double averageFillRate) {
}
