package com.heartbeatsend.heartbeat.entity;

import com.heartbeatsend.auth.entity.AppUser;
import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.OriginTag;
import com.heartbeatsend.heartbeat.domain.SourceKind;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "heartbeat_recording")
public class HeartbeatRecording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recording_id")
    private Long recordingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private AppUser owner;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_tag", nullable = false, length = 16)
    private OriginTag originTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_kind", nullable = false, length = 16)
    private SourceKind sourceKind;

    @Enumerated(EnumType.STRING)
    @Column(name = "capture_mode", nullable = false, length = 24)
    private CaptureMode captureMode;

    @Column(name = "sensor_origin", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean sensorOrigin;

    @Column(name = "non_sensor_label", length = 200)
    private String nonSensorLabel;

    @Column(name = "bpm_nominal", nullable = false)
    private int bpmNominal;

    @Enumerated(EnumType.STRING)
    @Column(name = "timbre_code", nullable = false, length = 16)
    private TimbreCode timbreCode;

    @Column(name = "curve_json", nullable = false, columnDefinition = "TEXT")
    private String curveJson;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "situation_code", length = 24)
    private String situationCode;

    @Column(name = "mood_code", length = 24)
    private String moodCode;

    @Column(name = "intensity_code", length = 16)
    private String intensityCode;

    @Column(name = "questionnaire_note", length = 500)
    private String questionnaireNote;

    @Column(name = "synth_model", length = 80)
    private String synthModel;

    @Column(name = "share_token", length = 32, columnDefinition = "CHAR(32)")
    private String shareToken;

    @Column(name = "share_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean shareEnabled;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant updatedAt;

    public Long getRecordingId() {
        return recordingId;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OriginTag getOriginTag() {
        return originTag;
    }

    public void setOriginTag(OriginTag originTag) {
        this.originTag = originTag;
    }

    public SourceKind getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(SourceKind sourceKind) {
        this.sourceKind = sourceKind;
    }

    public CaptureMode getCaptureMode() {
        return captureMode;
    }

    public void setCaptureMode(CaptureMode captureMode) {
        this.captureMode = captureMode;
    }

    public boolean isSensorOrigin() {
        return sensorOrigin;
    }

    public void setSensorOrigin(boolean sensorOrigin) {
        this.sensorOrigin = sensorOrigin;
    }

    public String getNonSensorLabel() {
        return nonSensorLabel;
    }

    public void setNonSensorLabel(String nonSensorLabel) {
        this.nonSensorLabel = nonSensorLabel;
    }

    public int getBpmNominal() {
        return bpmNominal;
    }

    public void setBpmNominal(int bpmNominal) {
        this.bpmNominal = bpmNominal;
    }

    public TimbreCode getTimbreCode() {
        return timbreCode;
    }

    public void setTimbreCode(TimbreCode timbreCode) {
        this.timbreCode = timbreCode;
    }

    public String getCurveJson() {
        return curveJson;
    }

    public void setCurveJson(String curveJson) {
        this.curveJson = curveJson;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getSituationCode() {
        return situationCode;
    }

    public void setSituationCode(String situationCode) {
        this.situationCode = situationCode;
    }

    public String getMoodCode() {
        return moodCode;
    }

    public void setMoodCode(String moodCode) {
        this.moodCode = moodCode;
    }

    public String getIntensityCode() {
        return intensityCode;
    }

    public void setIntensityCode(String intensityCode) {
        this.intensityCode = intensityCode;
    }

    public String getQuestionnaireNote() {
        return questionnaireNote;
    }

    public void setQuestionnaireNote(String questionnaireNote) {
        this.questionnaireNote = questionnaireNote;
    }

    public String getSynthModel() {
        return synthModel;
    }

    public void setSynthModel(String synthModel) {
        this.synthModel = synthModel;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public boolean isShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
