package com.heartbeatsend.heartbeat.controller;

import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.common.api.ApiException;
import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.dto.CreateCustomRequest;
import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import com.heartbeatsend.heartbeat.dto.CreateWearableMockRequest;
import com.heartbeatsend.heartbeat.dto.RecordingResponse;
import com.heartbeatsend.heartbeat.dto.ShareResponse;
import com.heartbeatsend.heartbeat.service.HeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeats")
@Tag(name = "Heartbeat")
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    public HeartbeatController(HeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @GetMapping
    @Operation(summary = "List the current user's library, optionally filtered by origin tag")
    public List<RecordingResponse> list(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(required = false) OriginTag originTag
    ) {
        requireUser(user);
        return heartbeatService.list(user, originTag);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one owned recording")
    public RecordingResponse get(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
        requireUser(user);
        return heartbeatService.getOwned(user, id);
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save a custom BPM / timbre / curve program")
    public RecordingResponse createCustom(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody CreateCustomRequest request
    ) {
        requireUser(user);
        return heartbeatService.createCustom(user, request);
    }

    @PostMapping("/synth")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Synthesize from a questionnaire (OpenAI if keyed, else rules). Always labeled non-sensor.")
    public RecordingResponse createSynth(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody CreateSynthRequest request
    ) {
        requireUser(user);
        return heartbeatService.createSynth(user, request);
    }

    @PostMapping("/wearable-mock")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Capture a mock wearable session (placeholder until a live device is wired)")
    public RecordingResponse createWearableMock(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody CreateWearableMockRequest request
    ) {
        requireUser(user);
        return heartbeatService.createWearableMock(user, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an owned recording")
    public void delete(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
        requireUser(user);
        heartbeatService.deleteOwned(user, id);
    }

    @PostMapping("/{id}/share")
    @Operation(summary = "Mint or re-enable an optional public share link")
    public ShareResponse enableShare(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
        requireUser(user);
        return heartbeatService.enableShare(user, id);
    }

    @DeleteMapping("/{id}/share")
    @Operation(summary = "Disable the public share link")
    public ShareResponse disableShare(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
        requireUser(user);
        return heartbeatService.disableShare(user, id);
    }

    private static void requireUser(AppUser user) {
        if (user == null) {
            throw ApiException.unauthorized("Not authenticated");
        }
    }
}
