package com.heartbeatsend.heartbeat.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CurvePoint(
        @Min(0) @Max(600) double tSeconds,
        @Min(40) @Max(220) int bpm
) {
}
