package com.heartbeatsend.heartbeat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.auth.repository.UserRepository;
import com.heartbeatsend.common.api.ApiException;
import com.heartbeatsend.common.config.HeartbeatProperties;
import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.domain.SourceKind;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import com.heartbeatsend.heartbeat.dto.CreateCustomRequest;
import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import com.heartbeatsend.heartbeat.dto.CreateWearableMockRequest;
import com.heartbeatsend.heartbeat.dto.RecordingResponse;
import com.heartbeatsend.heartbeat.dto.ShareResponse;
import com.heartbeatsend.heartbeat.entity.HeartbeatRecording;
import com.heartbeatsend.heartbeat.repository.HeartbeatRecordingRepository;
import com.heartbeatsend.heartbeat.synth.RuleSynthEngine;
import com.heartbeatsend.heartbeat.synth.SynthPlan;
import com.heartbeatsend.heartbeat.synth.SynthService;
import com.heartbeatsend.heartbeat.wearable.WearableHeartRateProvider;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeartbeatService {

    public static final String CUSTOM_NON_SENSOR_LABEL =
            "非传感器 / Non-sensor: custom BPM, timbre, and curve drawn by the user";

    private final HeartbeatRecordingRepository recordingRepository;
    private final UserRepository userRepository;
    private final SynthService synthService;
    private final WearableHeartRateProvider wearableHeartRateProvider;
    private final ObjectMapper objectMapper;
    private final HeartbeatProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public HeartbeatService(
            HeartbeatRecordingRepository recordingRepository,
            UserRepository userRepository,
            SynthService synthService,
            WearableHeartRateProvider wearableHeartRateProvider,
            ObjectMapper objectMapper,
            HeartbeatProperties properties
    ) {
        this.recordingRepository = recordingRepository;
        this.userRepository = userRepository;
        this.synthService = synthService;
        this.wearableHeartRateProvider = wearableHeartRateProvider;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<RecordingResponse> list(AppUser owner, OriginTag originTag) {
        List<HeartbeatRecording> rows = originTag == null
                ? recordingRepository.findByOwnerUserIdOrderByCreatedAtDesc(owner.getUserId())
                : recordingRepository.findByOwnerUserIdAndOriginTagOrderByCreatedAtDesc(owner.getUserId(), originTag);
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecordingResponse getOwned(AppUser owner, long recordingId) {
        return toResponse(requireOwned(owner, recordingId));
    }

    @Transactional(readOnly = true)
    public RecordingResponse getPublic(String shareToken) {
        HeartbeatRecording recording = recordingRepository
                .findByShareTokenAndShareEnabledTrue(shareToken)
                .orElseThrow(() -> ApiException.notFound("Share link is invalid or disabled"));
        return toResponse(recording);
    }

    @Transactional
    public RecordingResponse createCustom(AppUser owner, CreateCustomRequest request) {
        List<CurvePoint> curve = request.curve() == null || request.curve().isEmpty()
                ? List.of(
                        new CurvePoint(0, request.bpmNominal()),
                        new CurvePoint(request.durationSeconds(), request.bpmNominal())
                )
                : request.curve();
        HeartbeatRecording recording = baseRecording(owner);
        recording.setTitle(request.title().trim());
        recording.setOriginTag(OriginTag.CUSTOM);
        recording.setSourceKind(SourceKind.CUSTOM);
        recording.setCaptureMode(CaptureMode.USER_DEFINED);
        recording.setSensorOrigin(false);
        recording.setNonSensorLabel(CUSTOM_NON_SENSOR_LABEL);
        recording.setBpmNominal(request.bpmNominal());
        recording.setTimbreCode(request.timbreCode());
        recording.setCurveJson(RecordingMapper.writeCurve(curve, objectMapper));
        recording.setDurationSeconds(request.durationSeconds());
        return toResponse(recordingRepository.save(recording));
    }

    @Transactional
    public RecordingResponse createSynth(AppUser owner, CreateSynthRequest request) {
        String situation = RuleSynthEngine.normalize(
                request.situationCode(),
                java.util.Set.of("REST", "WALKING", "EXERCISE", "ANXIOUS", "SLEEP", "TENDER", "CUSTOM"),
                "REST"
        );
        String mood = RuleSynthEngine.normalize(
                request.moodCode(),
                java.util.Set.of("CALM", "EXCITED", "STRESSED", "TENDER", "ENERGETIC"),
                "CALM"
        );
        String intensity = RuleSynthEngine.normalize(
                request.intensityCode(),
                java.util.Set.of("LOW", "MEDIUM", "HIGH"),
                "MEDIUM"
        );
        SynthPlan plan = synthService.synthesize(request);
        HeartbeatRecording recording = baseRecording(owner);
        recording.setTitle(plan.title());
        recording.setOriginTag(OriginTag.GENERATED);
        recording.setSourceKind(SourceKind.SYNTH);
        recording.setCaptureMode(plan.captureMode());
        recording.setSensorOrigin(false);
        recording.setNonSensorLabel(plan.nonSensorLabel());
        recording.setBpmNominal(plan.bpmNominal());
        recording.setTimbreCode(plan.timbreCode());
        recording.setCurveJson(RecordingMapper.writeCurve(plan.curve(), objectMapper));
        recording.setDurationSeconds(request.durationSeconds());
        recording.setSituationCode(situation);
        recording.setMoodCode(mood);
        recording.setIntensityCode(intensity);
        recording.setQuestionnaireNote(blankToNull(request.note()));
        recording.setSynthModel(plan.synthModel());
        return toResponse(recordingRepository.save(recording));
    }

    @Transactional
    public RecordingResponse createWearableMock(AppUser owner, CreateWearableMockRequest request) {
        int duration = request.durationSeconds() == null || request.durationSeconds() <= 0
                ? 45
                : request.durationSeconds();
        WearableHeartRateProvider.WearableSample sample = wearableHeartRateProvider.captureMockSession(duration);
        String title = request.title() == null || request.title().isBlank()
                ? "Wearable mock · " + sample.bpmNominal() + " BPM"
                : request.title().trim();
        HeartbeatRecording recording = baseRecording(owner);
        recording.setTitle(title);
        recording.setOriginTag(OriginTag.MEASURED);
        recording.setSourceKind(SourceKind.WEARABLE);
        recording.setCaptureMode(CaptureMode.WEARABLE_MOCK);
        recording.setSensorOrigin(false);
        recording.setNonSensorLabel(sample.sourceNote());
        recording.setBpmNominal(sample.bpmNominal());
        recording.setTimbreCode(TimbreCode.HEART);
        recording.setCurveJson(RecordingMapper.writeCurve(sample.curve(), objectMapper));
        recording.setDurationSeconds(duration);
        return toResponse(recordingRepository.save(recording));
    }

    @Transactional
    public void deleteOwned(AppUser owner, long recordingId) {
        HeartbeatRecording recording = requireOwned(owner, recordingId);
        recordingRepository.delete(recording);
    }

    @Transactional
    public ShareResponse enableShare(AppUser owner, long recordingId) {
        HeartbeatRecording recording = requireOwned(owner, recordingId);
        if (recording.getShareToken() == null) {
            recording.setShareToken(newToken());
        }
        recording.setShareEnabled(true);
        recording.setUpdatedAt(Instant.now());
        recordingRepository.save(recording);
        return shareOf(recording);
    }

    @Transactional
    public ShareResponse disableShare(AppUser owner, long recordingId) {
        HeartbeatRecording recording = requireOwned(owner, recordingId);
        recording.setShareEnabled(false);
        recording.setUpdatedAt(Instant.now());
        recordingRepository.save(recording);
        return new ShareResponse(false, null, null);
    }

    private HeartbeatRecording requireOwned(AppUser owner, long recordingId) {
        return recordingRepository.findByRecordingIdAndOwnerUserId(recordingId, owner.getUserId())
                .orElseThrow(() -> ApiException.notFound("Recording not found"));
    }

    private HeartbeatRecording baseRecording(AppUser owner) {
        Instant now = Instant.now();
        HeartbeatRecording recording = new HeartbeatRecording();
        recording.setOwner(userRepository.getReferenceById(owner.getUserId()));
        recording.setShareEnabled(false);
        recording.setCreatedAt(now);
        recording.setUpdatedAt(now);
        return recording;
    }

    private RecordingResponse toResponse(HeartbeatRecording recording) {
        return RecordingMapper.toResponse(recording, objectMapper, properties);
    }

    private ShareResponse shareOf(HeartbeatRecording recording) {
        String url = properties.getPublicBaseUrl() + "/s/" + recording.getShareToken();
        return new ShareResponse(true, recording.getShareToken(), url);
    }

    private String newToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
