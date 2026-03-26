package com.eventflow.platform.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Set;

public record UpdateEventRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 300) String summary,
        @NotBlank String description,
        @Size(max = 500) String coverImageUrl,
        @NotBlank @Size(max = 200) String locationName,
        @Size(max = 255) String address,
        @NotBlank @Size(max = 100) String city,
        @NotNull @Future OffsetDateTime startTime,
        @NotNull @Future OffsetDateTime endTime,
        @NotNull @Future OffsetDateTime registrationDeadline,
        @NotNull @Min(1) Integer capacity,
        Boolean featured,
        @NotBlank String categoryCode,
        Set<String> tags) {
}
