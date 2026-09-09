package com.heartbeatsend.heartbeat.wearable;

import com.heartbeatsend.heartbeat.domain.CurvePoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class MockWearableHeartRateProvider implements WearableHeartRateProvider {

    public static final String MOCK_NON_SENSOR_LABEL =
            "非传感器 / Non-sensor: wearable mock (placeholder). Not a live device reading.";

    @Override
    public String providerName() {
        return "mock-wearable";
    }

    @Override
    public boolean isLiveDeviceConnected() {
        return false;
    }

    @Override
    public WearableSample captureMockSession(int durationSeconds) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int baseline = rng.nextInt(64, 86);
        List<CurvePoint> curve = new ArrayList<>();
        int steps = Math.max(6, durationSeconds / 5);
        int sum = 0;
        for (int i = 0; i <= steps; i++) {
            double t = durationSeconds * (i / (double) steps);
            int wander = (int) Math.round(4 * Math.sin(i * 0.8) + rng.nextInt(-2, 3));
            int bpm = Math.min(180, Math.max(48, baseline + wander));
            curve.add(new CurvePoint(Math.round(t * 10.0) / 10.0, bpm));
            sum += bpm;
        }
        int nominal = Math.round(sum / (float) curve.size());
        return new WearableSample(nominal, curve, false, MOCK_NON_SENSOR_LABEL);
    }

    @Override
    public Optional<WearableSample> captureLiveSession(int durationSeconds) {
        return Optional.empty();
    }
}
