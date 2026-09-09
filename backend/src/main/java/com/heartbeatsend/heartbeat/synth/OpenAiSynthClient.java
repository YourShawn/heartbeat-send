package com.heartbeatsend.heartbeat.synth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartbeatsend.common.config.HeartbeatProperties;
import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiSynthClient {

    public static final String NON_SENSOR_LABEL =
            "非传感器 / Non-sensor: synthesized by an OpenAI-compatible model, not a wearable reading";

    private static final Logger log = LoggerFactory.getLogger(OpenAiSynthClient.class);

    private final HeartbeatProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiSynthClient(HeartbeatProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public Optional<SynthPlan> tryPlan(CreateSynthRequest request) {
        if (!properties.getOpenai().isConfigured()) {
            return Optional.empty();
        }
        try {
            String content = complete(request);
            return Optional.of(parsePlan(content, request, properties.getOpenai().getModel()));
        } catch (Exception ex) {
            log.warn("OpenAI synth failed, falling back to rules: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String complete(CreateSynthRequest request) {
        String base = properties.getOpenai().getBaseUrl().replaceAll("/$", "");
        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(Map.of(
                    "model", properties.getOpenai().getModel(),
                    "temperature", 0.4,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", userPrompt(request))
                    )
            ));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode OpenAI request", e);
        }

        String raw = restClient.post()
                .uri(base + "/chat/completions")
                .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyJson)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(raw);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI response", e);
        }
    }

    private SynthPlan parsePlan(String content, CreateSynthRequest request, String model) throws Exception {
        JsonNode node = objectMapper.readTree(content);
        int bpm = clamp(node.path("bpm").asInt(72), 40, 220);
        TimbreCode timbre = parseTimbre(node.path("timbre").asText("HEART"));
        int duration = request.durationSeconds();
        List<CurvePoint> curve = new ArrayList<>();
        JsonNode curveNode = node.path("curve");
        if (curveNode.isArray() && curveNode.size() >= 2) {
            for (JsonNode p : curveNode) {
                curve.add(new CurvePoint(
                        p.path("tSeconds").asDouble(p.path("t").asDouble(0)),
                        clamp(p.path("bpm").asInt(bpm), 40, 220)
                ));
            }
        } else {
            curve.add(new CurvePoint(0, bpm));
            curve.add(new CurvePoint(duration, bpm));
        }
        String title = node.path("title").asText(null);
        if (title == null || title.isBlank()) {
            title = request.title() == null || request.title().isBlank()
                    ? "AI pulse · " + request.situationCode()
                    : request.title().trim();
        }
        return new SynthPlan(title, bpm, timbre, curve, CaptureMode.OPENAI_SYNTH, model, NON_SENSOR_LABEL);
    }

    private static TimbreCode parseTimbre(String raw) {
        try {
            return TimbreCode.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return TimbreCode.HEART;
        }
    }

    private static String systemPrompt() {
        return """
                You design aesthetic heartbeat playback programs for a listening app.
                This is NOT medical advice and must never claim to be a real sensor reading.
                Return JSON: {"title": string, "bpm": int 40-180, "timbre": "SINE"|"HEART"|"DRUM"|"SOFT",
                "curve": [{"tSeconds": number, "bpm": int}]}.
                Keep the curve smooth. Match the situation, mood, and intensity.
                """;
    }

    private static String userPrompt(CreateSynthRequest request) {
        return "situation=" + request.situationCode()
                + " mood=" + request.moodCode()
                + " intensity=" + request.intensityCode()
                + " durationSeconds=" + request.durationSeconds()
                + " note=" + (request.note() == null ? "" : request.note());
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
}
