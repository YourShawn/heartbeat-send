package com.heartbeatsend.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorBody(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<String> details
) {
}
