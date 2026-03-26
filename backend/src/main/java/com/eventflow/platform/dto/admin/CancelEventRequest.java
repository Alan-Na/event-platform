package com.eventflow.platform.dto.admin;

import jakarta.validation.constraints.Size;

public record CancelEventRequest(@Size(max = 255) String reason) {
}
