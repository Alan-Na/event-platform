package com.eventflow.platform.dto.checkin;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(@NotBlank String confirmationCode) {
}
