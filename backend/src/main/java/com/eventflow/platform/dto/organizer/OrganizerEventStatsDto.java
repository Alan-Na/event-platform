package com.eventflow.platform.dto.organizer;

import com.eventflow.platform.dto.ticket.TicketTypeSummaryDto;
import java.time.OffsetDateTime;
import java.util.List;

public record OrganizerEventStatsDto(
        Long eventId,
        String eventTitle,
        String status,
        Integer capacity,
        Integer confirmedCount,
        Integer waitlistCount,
        long checkedInCount,
        OffsetDateTime startTime,
        List<TicketTypeSummaryDto> ticketTypes) {
}
