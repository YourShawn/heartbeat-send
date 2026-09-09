package com.heartbeatsend.heartbeat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateWearableMockRequest(
        @Size(max = 120) String title,
        @Min(5) @Max(600) Integer durationSeconds
) {
}
