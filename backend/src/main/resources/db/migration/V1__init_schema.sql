-- Heartbeat Send schema. One semantic per column. See docs/schema.md.

CREATE TABLE app_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key for a login account',
    username VARCHAR(64) NOT NULL COMMENT 'Unique login name, stored lowercase',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt hash of the password; never store plaintext',
    display_name VARCHAR(80) NOT NULL COMMENT 'Name shown in the library UI',
    created_at DATETIME(3) NOT NULL COMMENT 'UTC timestamp when the account was created',
    updated_at DATETIME(3) NOT NULL COMMENT 'UTC timestamp when the account row was last changed',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='Login accounts for Heartbeat Send';

CREATE TABLE heartbeat_recording (
    recording_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key for one saved pulse program',
    owner_user_id BIGINT NOT NULL COMMENT 'FK to app_user.user_id; the account that owns this recording',
    title VARCHAR(120) NOT NULL COMMENT 'Human title for the library card',
    origin_tag VARCHAR(16) NOT NULL COMMENT 'Library tag: CUSTOM, GENERATED, or MEASURED (自定义/生成/真测)',
    source_kind VARCHAR(16) NOT NULL COMMENT 'Product source path: CUSTOM, SYNTH, or WEARABLE',
    capture_mode VARCHAR(24) NOT NULL COMMENT 'How values were produced: USER_DEFINED, RULE_SYNTH, OPENAI_SYNTH, WEARABLE_MOCK, WEARABLE_LIVE',
    sensor_origin TINYINT(1) NOT NULL COMMENT '1 only when BPM came from a live wearable sensor; otherwise 0',
    non_sensor_label VARCHAR(200) NULL COMMENT 'Required when sensor_origin=0; UI text stating this is not sensor data',
    bpm_nominal INT NOT NULL COMMENT 'Representative BPM used as the local playback baseline (40-220)',
    timbre_code VARCHAR(16) NOT NULL COMMENT 'Playback timbre: SINE, HEART, DRUM, or SOFT',
    curve_json TEXT NOT NULL COMMENT 'JSON array of {tSeconds, bpm} control points for local Web Audio playback',
    duration_seconds INT NOT NULL COMMENT 'Intended playback length in whole seconds',
    situation_code VARCHAR(24) NULL COMMENT 'Questionnaire situation code; null when not from synth',
    mood_code VARCHAR(24) NULL COMMENT 'Questionnaire mood code; null when not from synth',
    intensity_code VARCHAR(16) NULL COMMENT 'Questionnaire intensity code; null when not from synth',
    questionnaire_note VARCHAR(500) NULL COMMENT 'Optional free-text situation note sent to the synth',
    synth_model VARCHAR(80) NULL COMMENT 'OpenAI-compatible model id used; null for rule engine or non-synth',
    share_token CHAR(32) NULL COMMENT 'Unguessable public share token; null when sharing is off',
    share_enabled TINYINT(1) NOT NULL COMMENT '1 if share_token may be used by anonymous listeners',
    created_at DATETIME(3) NOT NULL COMMENT 'UTC insert time',
    updated_at DATETIME(3) NOT NULL COMMENT 'UTC last change time',
    PRIMARY KEY (recording_id),
    UNIQUE KEY uk_heartbeat_share_token (share_token),
    KEY ix_heartbeat_owner_created (owner_user_id, created_at),
    KEY ix_heartbeat_owner_origin (owner_user_id, origin_tag),
    CONSTRAINT fk_heartbeat_owner FOREIGN KEY (owner_user_id) REFERENCES app_user (user_id),
    CONSTRAINT chk_heartbeat_origin_tag CHECK (origin_tag IN ('CUSTOM', 'GENERATED', 'MEASURED')),
    CONSTRAINT chk_heartbeat_source_kind CHECK (source_kind IN ('CUSTOM', 'SYNTH', 'WEARABLE')),
    CONSTRAINT chk_heartbeat_capture_mode CHECK (capture_mode IN (
        'USER_DEFINED', 'RULE_SYNTH', 'OPENAI_SYNTH', 'WEARABLE_MOCK', 'WEARABLE_LIVE'
    )),
    CONSTRAINT chk_heartbeat_sensor_origin CHECK (sensor_origin IN (0, 1)),
    CONSTRAINT chk_heartbeat_sensor_live_only CHECK (sensor_origin = 0 OR capture_mode = 'WEARABLE_LIVE'),
    CONSTRAINT chk_heartbeat_non_sensor_label CHECK (
        sensor_origin = 1 OR (non_sensor_label IS NOT NULL AND CHAR_LENGTH(non_sensor_label) > 0)
    ),
    CONSTRAINT chk_heartbeat_share CHECK (share_enabled IN (0, 1)),
    CONSTRAINT chk_heartbeat_share_token CHECK (share_enabled = 0 OR share_token IS NOT NULL),
    CONSTRAINT chk_heartbeat_bpm CHECK (bpm_nominal BETWEEN 40 AND 220),
    CONSTRAINT chk_heartbeat_duration CHECK (duration_seconds BETWEEN 5 AND 600),
    CONSTRAINT chk_heartbeat_timbre CHECK (timbre_code IN ('SINE', 'HEART', 'DRUM', 'SOFT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='Saved heartbeat programs for local playback; not medical records';
