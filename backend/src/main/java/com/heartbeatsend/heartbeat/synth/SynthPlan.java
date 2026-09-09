package com.heartbeatsend.heartbeat.synth;

import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import java.util.List;

public record SynthPlan(
        String title,
        int bpmNominal,
        TimbreCode timbreCode,
        List<CurvePoint> curve,
        CaptureMode captureMode,
        String synthModel,
        String nonSensorLabel
) {
}
