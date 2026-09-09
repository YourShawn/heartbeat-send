package com.heartbeatsend.heartbeat.repository;

import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.entity.HeartbeatRecording;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartbeatRecordingRepository extends JpaRepository<HeartbeatRecording, Long> {

    List<HeartbeatRecording> findByOwnerUserIdOrderByCreatedAtDesc(long ownerUserId);

    List<HeartbeatRecording> findByOwnerUserIdAndOriginTagOrderByCreatedAtDesc(long ownerUserId, OriginTag originTag);

    Optional<HeartbeatRecording> findByRecordingIdAndOwnerUserId(long recordingId, long ownerUserId);

    Optional<HeartbeatRecording> findByShareTokenAndShareEnabledTrue(String shareToken);
}
