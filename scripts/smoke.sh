#!/usr/bin/env bash
# Live HTTP smoke against a running backend (default http://localhost:8080).
set -euo pipefail
BASE="${1:-http://localhost:8080}"

echo "== health =="
curl -fsS "$BASE/actuator/health" | grep -q UP

echo "== login =="
LOGIN=$(curl -fsS -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo123"}' \
  "$BASE/api/auth/login")
TOKEN=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])' <<<"$LOGIN")

echo "== custom =="
CUSTOM=$(curl -fsS -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Smoke custom","bpmNominal":74,"timbreCode":"HEART","durationSeconds":20,"curve":[{"tSeconds":0,"bpm":70},{"tSeconds":20,"bpm":78}]}' \
  "$BASE/api/heartbeats/custom")
ID=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["recordingId"])' <<<"$CUSTOM")

echo "== synth (rules fallback) =="
curl -fsS -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"situationCode":"SLEEP","moodCode":"CALM","intensityCode":"LOW","durationSeconds":15}' \
  "$BASE/api/heartbeats/synth" | grep -q 'GENERATED'

echo "== wearable mock =="
curl -fsS -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"durationSeconds":15}' \
  "$BASE/api/heartbeats/wearable-mock" | grep -q 'WEARABLE_MOCK'

echo "== library =="
curl -fsS -H "Authorization: Bearer $TOKEN" "$BASE/api/heartbeats" | grep -q recordingId

echo "== share =="
SHARE=$(curl -fsS -H "Authorization: Bearer $TOKEN" -X POST "$BASE/api/heartbeats/$ID/share")
TOKEN_SHARE=$(python3 -c 'import json,sys; print(json.load(sys.stdin)["shareToken"])' <<<"$SHARE")
curl -fsS "$BASE/api/public/heartbeats/$TOKEN_SHARE" | grep -q 'Smoke custom'

echo "== openapi =="
curl -fsS "$BASE/v3/api-docs" | grep -q 'Heartbeat Send'

echo "SMOKE OK"
