package com.heartbeatsend.heartbeat.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSynthRequest(
        @Size(max = 120) String title,
        @NotBlank String situationCode,
        @NotBlank String moodCode,
        @NotBlank String intensityCode,
        @Min(5) @Max(600) int durationSeconds,
        @Size(max = 500) String note
) {
}
