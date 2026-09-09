package com.heartbeatsend.heartbeat.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.auth.repository.UserRepository;
import com.heartbeatsend.auth.seed.DemoUserSeeder;
import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.domain.SourceKind;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import com.heartbeatsend.heartbeat.entity.HeartbeatRecording;
import com.heartbeatsend.heartbeat.repository.HeartbeatRecordingRepository;
import com.heartbeatsend.heartbeat.service.HeartbeatService;
import com.heartbeatsend.heartbeat.service.RecordingMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class DemoLibrarySeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final HeartbeatRecordingRepository recordingRepository;
    private final ObjectMapper objectMapper;

    public DemoLibrarySeeder(
            UserRepository userRepository,
            HeartbeatRecordingRepository recordingRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.recordingRepository = recordingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppUser demo = userRepository.findByUsername(DemoUserSeeder.DEMO_USERNAME).orElse(null);
        if (demo == null) {
            return;
        }
        if (!recordingRepository.findByOwnerUserIdOrderByCreatedAtDesc(demo.getUserId()).isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        HeartbeatRecording sample = new HeartbeatRecording();
        sample.setOwner(demo);
        sample.setTitle("Evening quiet / 晚间静心");
        sample.setOriginTag(OriginTag.CUSTOM);
        sample.setSourceKind(SourceKind.CUSTOM);
        sample.setCaptureMode(CaptureMode.USER_DEFINED);
        sample.setSensorOrigin(false);
        sample.setNonSensorLabel(HeartbeatService.CUSTOM_NON_SENSOR_LABEL);
        sample.setBpmNominal(66);
        sample.setTimbreCode(TimbreCode.SOFT);
        sample.setCurveJson(RecordingMapper.writeCurve(
                List.of(new CurvePoint(0, 70), new CurvePoint(20, 66), new CurvePoint(45, 62)),
                objectMapper
        ));
        sample.setDurationSeconds(45);
        sample.setShareEnabled(false);
        sample.setCreatedAt(now);
        sample.setUpdatedAt(now);
        recordingRepository.save(sample);
    }
}
