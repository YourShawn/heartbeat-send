package com.heartbeatsend.heartbeat.dto;

import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCustomRequest(
        @NotBlank @Size(max = 120) String title,
        @Min(40) @Max(220) int bpmNominal,
        @NotNull TimbreCode timbreCode,
        @Min(5) @Max(600) int durationSeconds,
        @Valid List<CurvePoint> curve
) {
}
