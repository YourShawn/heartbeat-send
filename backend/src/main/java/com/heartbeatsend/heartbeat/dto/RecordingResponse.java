package com.heartbeatsend.heartbeat.dto;

import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.domain.SourceKind;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import java.time.Instant;
import java.util.List;

public record RecordingResponse(
        long recordingId,
        String title,
        OriginTag originTag,
        String originTagZh,
        String originTagEn,
        SourceKind sourceKind,
        CaptureMode captureMode,
        boolean sensorOrigin,
        String nonSensorLabel,
        int bpmNominal,
        TimbreCode timbreCode,
        List<CurvePoint> curve,
        int durationSeconds,
        String situationCode,
        String moodCode,
        String intensityCode,
        String questionnaireNote,
        String synthModel,
        boolean shareEnabled,
        String shareToken,
        String shareUrl,
        Instant createdAt
) {
}
