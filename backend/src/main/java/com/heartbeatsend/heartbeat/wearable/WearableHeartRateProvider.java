package com.heartbeatsend.heartbeat.wearable;

import com.heartbeatsend.heartbeat.domain.CurvePoint;
import java.util.List;
import java.util.Optional;

/**
 * Placeholder for a future live wearable (BLE / HealthKit / Health Connect).
 * Today only {@link #captureMockSession(int)} is implemented.
 */
public interface WearableHeartRateProvider {

    String providerName();

    boolean isLiveDeviceConnected();

    WearableSample captureMockSession(int durationSeconds);

    Optional<WearableSample> captureLiveSession(int durationSeconds);

    record WearableSample(int bpmNominal, List<CurvePoint> curve, boolean liveSensor, String sourceNote) {
    }
}
