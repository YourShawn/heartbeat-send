package com.heartbeatsend.heartbeat.controller;

import com.heartbeatsend.heartbeat.dto.RecordingResponse;
import com.heartbeatsend.heartbeat.service.HeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public")
public class PublicHeartbeatController {

    private final HeartbeatService heartbeatService;

    public PublicHeartbeatController(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @GetMapping("/heartbeats/{shareToken}")
    @SecurityRequirements
    @Operation(summary = "Play a shared recording without signing in")
    public RecordingResponse getShared(@PathVariable String shareToken) {
        return heartbeatService.getPublic(shareToken);
    }
}
