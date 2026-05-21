package com.eventflow.platform.mapper;

import com.eventflow.platform.dto.event.CurrentUserRegistrationDto;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.dto.ticket.TicketTypeSummaryDto;
import com.eventflow.platform.entity.Event;
import com.eventflow.platform.entity.TicketType;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventSummaryDto toSummaryDto(Event event) {
        Set<String> tags = event.getTags() == null ? Set.of() : Set.copyOf(event.getTags());
        return new EventSummaryDto(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getSummary(),
                event.getCoverImageUrl(),
                event.getCity(),
                event.getLocationName(),
                event.getStartTime(),
                event.getEndTime(),
                event.getRegistrationDeadline(),
                event.getCapacity(),
                event.getConfirmedCount(),
                event.getCapacity() - event.getConfirmedCount(),
                event.getFeatured(),
                event.getStatus(),
                event.getCategory() != null ? event.getCategory().getCode() : null,
                tags);
    }

    public EventDetailDto toDetailDto(Event event, boolean bookable, String reason,
            CurrentUserRegistrationDto currentRegistration, List<TicketType> ticketTypes) {
        Set<String> tags = event.getTags() == null ? Set.of() : Set.copyOf(event.getTags());
        List<TicketTypeSummaryDto> ticketTypeDtos = ticketTypes == null ? List.of()
                : ticketTypes.stream().map(this::toTicketTypeSummary).toList();
        return new EventDetailDto(
                event.getId(),
                event.getSlug(),
                event.getTitle(),
                event.getSummary(),
                event.getDescription(),
                event.getCoverImageUrl(),
                event.getCity(),
                event.getLocationName(),
                event.getAddress(),
                event.getStartTime(),
                event.getEndTime(),
                event.getRegistrationDeadline(),
                event.getCapacity(),
                event.getConfirmedCount(),
                event.getCapacity() - event.getConfirmedCount(),
                event.getWaitlistCount(),
                event.getFeatured(),
                event.getStatus(),
                event.getCategory() != null ? event.getCategory().getCode() : null,
                tags,
                bookable,
                reason,
                currentRegistration,
                ticketTypeDtos);
    }

    public TicketTypeSummaryDto toTicketTypeSummary(TicketType t) {
        return new TicketTypeSummaryDto(
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.getPriceAmount(),
                t.getCurrency(),
                t.getCapacity(),
                t.getConfirmedCount(),
                t.getCapacity() - t.getConfirmedCount(),
                t.getSalesStartAt(),
                t.getSalesEndAt(),
                t.getActive());
    }
}
