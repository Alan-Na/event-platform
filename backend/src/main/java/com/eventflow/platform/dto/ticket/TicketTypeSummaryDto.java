package com.eventflow.platform.dto.ticket;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TicketTypeSummaryDto(
        Long id,
        String name,
        String description,
        BigDecimal priceAmount,
        String currency,
        Integer capacity,
        Integer confirmedCount,
        Integer remainingSeats,
        OffsetDateTime salesStartAt,
        OffsetDateTime salesEndAt,
        Boolean active) {
}
