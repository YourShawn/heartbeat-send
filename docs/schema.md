# Database schema — Heartbeat Send

This document is the semantic contract for Flyway migration `V1__init_schema.sql`.
Each column has **one meaning**. Do not overload columns.

## Conventions

- Engine: InnoDB, `utf8mb4_0900_ai_ci`.
- Timestamps are UTC `DATETIME(3)`.
- Enumerations are stored as `VARCHAR` with `CHECK` constraints (stable, readable).
- Primary keys are surrogate `BIGINT` identity columns.
- Foreign keys are explicit; recordings cannot exist without an owner.

## `app_user`

Login account. Not a medical identity.

| Column | Meaning |
| --- | --- |
| `user_id` | Surrogate primary key. |
| `username` | Unique login name, stored lowercase. |
| `password_hash` | BCrypt hash only. Never plaintext. |
| `display_name` | Name shown in the library UI. |
| `created_at` | UTC insert time. |
| `updated_at` | UTC last change time. |

Constraints: `PRIMARY KEY (user_id)`, `UNIQUE (username)`.

## `heartbeat_recording`

One saved **playback program** (BPM + timbre + curve). The server never stores audio files; the browser synthesizes sound locally.

| Column | Meaning |
| --- | --- |
| `recording_id` | Surrogate primary key. |
| `owner_user_id` | FK → `app_user.user_id`. Owner of this row. |
| `title` | Library card title. |
| `origin_tag` | Library tag: `CUSTOM` 自定义, `GENERATED` 生成, `MEASURED` 真测. |
| `source_kind` | Product path: `CUSTOM`, `SYNTH`, or `WEARABLE`. |
| `capture_mode` | How values were produced: `USER_DEFINED`, `RULE_SYNTH`, `OPENAI_SYNTH`, `WEARABLE_MOCK`, `WEARABLE_LIVE`. |
| `sensor_origin` | `1` **only** when BPM came from a live wearable sensor. Mock, synth, and custom are always `0`. |
| `non_sensor_label` | Required when `sensor_origin = 0`. UI text that this is not sensor data. |
| `bpm_nominal` | Representative BPM for playback (40–220). |
| `timbre_code` | Playback voice: `SINE`, `HEART`, `DRUM`, `SOFT`. |
| `curve_json` | JSON array of `{tSeconds, bpm}` control points. |
| `duration_seconds` | Intended playback length. |
| `situation_code` | Questionnaire situation; null if not synth. |
| `mood_code` | Questionnaire mood; null if not synth. |
| `intensity_code` | Questionnaire intensity; null if not synth. |
| `questionnaire_note` | Optional free-text note sent to synth. |
| `synth_model` | OpenAI-compatible model id; null for rules or non-synth. |
| `share_token` | Unguessable public token; null when unused. |
| `share_enabled` | `1` if anonymous listeners may use the token. |
| `created_at` | UTC insert time. |
| `updated_at` | UTC last change time. |

### Integrity rules

- `sensor_origin = 1` only if `capture_mode = 'WEARABLE_LIVE'`.
- If `sensor_origin = 0`, `non_sensor_label` must be non-empty.
- If `share_enabled = 1`, `share_token` must be present.
- `fk_heartbeat_owner` prevents orphan recordings.

## Origin mapping

| Source | `source_kind` | `origin_tag` | `sensor_origin` |
| --- | --- | --- | --- |
| Custom BPM / timbre / curve | `CUSTOM` | `CUSTOM` 自定义 | 0 |
| Questionnaire rule or OpenAI synth | `SYNTH` | `GENERATED` 生成 | 0 |
| Wearable mock (current) | `WEARABLE` | `MEASURED` 真测 | 0 |
| Wearable live (future) | `WEARABLE` | `MEASURED` 真测 | 1 |

## ER sketch

```
app_user 1 ──< heartbeat_recording
```
