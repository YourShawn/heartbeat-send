package com.heartbeatsend.heartbeat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartbeatsend.common.config.HeartbeatProperties;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.dto.RecordingResponse;
import com.heartbeatsend.heartbeat.entity.HeartbeatRecording;
import java.util.List;

public final class RecordingMapper {

    private static final TypeReference<List<CurvePoint>> CURVE_TYPE = new TypeReference<>() {
    };

    private RecordingMapper() {
    }

    public static RecordingResponse toResponse(
            HeartbeatRecording recording,
            ObjectMapper objectMapper,
            HeartbeatProperties properties
    ) {
        List<CurvePoint> curve = readCurve(recording.getCurveJson(), objectMapper);
        String shareUrl = null;
        if (recording.isShareEnabled() && recording.getShareToken() != null) {
            shareUrl = properties.getPublicBaseUrl() + "/s/" + recording.getShareToken();
        }
        return new RecordingResponse(
                recording.getRecordingId(),
                recording.getTitle(),
                recording.getOriginTag(),
                recording.getOriginTag().zhLabel(),
                recording.getOriginTag().enLabel(),
                recording.getSourceKind(),
                recording.getCaptureMode(),
                recording.isSensorOrigin(),
                recording.getNonSensorLabel(),
                recording.getBpmNominal(),
                recording.getTimbreCode(),
                curve,
                recording.getDurationSeconds(),
                recording.getSituationCode(),
                recording.getMoodCode(),
                recording.getIntensityCode(),
                recording.getQuestionnaireNote(),
                recording.getSynthModel(),
                recording.isShareEnabled(),
                recording.isShareEnabled() ? recording.getShareToken() : null,
                shareUrl,
                recording.getCreatedAt()
        );
    }

    public static String writeCurve(List<CurvePoint> curve, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(curve);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize BPM curve", e);
        }
    }

    public static List<CurvePoint> readCurve(String json, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, CURVE_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse BPM curve", e);
        }
    }
}
