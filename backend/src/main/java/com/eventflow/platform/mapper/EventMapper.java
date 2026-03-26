package com.eventflow.platform.mapper;

import com.eventflow.platform.dto.event.CurrentUserRegistrationDto;
import com.eventflow.platform.dto.event.EventDetailDto;
import com.eventflow.platform.dto.event.EventSummaryDto;
import com.eventflow.platform.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventSummaryDto toSummaryDto(Event event) {
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
                event.getTags());
    }

    public EventDetailDto toDetailDto(Event event, boolean bookable, String reason, CurrentUserRegistrationDto currentRegistration) {
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
                event.getTags(),
                bookable,
                reason,
                currentRegistration);
    }
}
