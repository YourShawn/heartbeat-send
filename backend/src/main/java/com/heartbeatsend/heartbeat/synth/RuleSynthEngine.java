package com.heartbeatsend.heartbeat.synth;

import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.CurvePoint;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Deterministic questionnaire → BPM/timbre/curve. Used when no OpenAI key is set,
 * and as a fallback if the remote model fails.
 */
@Component
public class RuleSynthEngine {

    public static final String NON_SENSOR_LABEL =
            "非传感器 / Non-sensor: synthesized from questionnaire (rule engine), not a wearable reading";

    private static final Set<String> SITUATIONS =
            Set.of("REST", "WALKING", "EXERCISE", "ANXIOUS", "SLEEP", "TENDER", "CUSTOM");
    private static final Set<String> MOODS =
            Set.of("CALM", "EXCITED", "STRESSED", "TENDER", "ENERGETIC");
    private static final Set<String> INTENSITIES = Set.of("LOW", "MEDIUM", "HIGH");

    public SynthPlan plan(CreateSynthRequest request) {
        String situation = normalize(request.situationCode(), SITUATIONS, "REST");
        String mood = normalize(request.moodCode(), MOODS, "CALM");
        String intensity = normalize(request.intensityCode(), INTENSITIES, "MEDIUM");

        int bpm = baseBpm(situation);
        bpm += moodShift(mood);
        bpm += intensityShift(intensity);
        bpm = clamp(bpm, 48, 168);

        TimbreCode timbre = timbreFor(situation, mood);
        List<CurvePoint> curve = curveFor(situation, mood, bpm, request.durationSeconds());
        String title = (request.title() == null || request.title().isBlank())
                ? defaultTitle(situation, mood) + " · " + bpm + " BPM"
                : request.title().trim().replaceAll("(?i)\\d+\\s*BPM", bpm + " BPM");
        if (request.title() != null && !request.title().isBlank() && !title.matches("(?i).*\\d+\\s*BPM.*")) {
            title = title + " · " + bpm + " BPM";
        }

        return new SynthPlan(
                title,
                bpm,
                timbre,
                curve,
                CaptureMode.RULE_SYNTH,
                null,
                NON_SENSOR_LABEL
        );
    }

    public static String normalize(String raw, Set<String> allowed, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        return allowed.contains(value) ? value : fallback;
    }

    private static int baseBpm(String situation) {
        return switch (situation) {
            case "SLEEP" -> 54;
            case "REST" -> 64;
            case "TENDER" -> 68;
            case "WALKING" -> 82;
            case "ANXIOUS" -> 92;
            case "EXERCISE" -> 128;
            case "CUSTOM" -> 76;
            default -> 72;
        };
    }

    private static int moodShift(String mood) {
        return switch (mood) {
            case "CALM" -> -4;
            case "TENDER" -> -2;
            case "EXCITED" -> 10;
            case "ENERGETIC" -> 14;
            case "STRESSED" -> 8;
            default -> 0;
        };
    }

    private static int intensityShift(String intensity) {
        return switch (intensity) {
            case "LOW" -> -6;
            case "HIGH" -> 8;
            default -> 0;
        };
    }

    private static TimbreCode timbreFor(String situation, String mood) {
        if ("EXERCISE".equals(situation) || "ENERGETIC".equals(mood)) {
            return TimbreCode.DRUM;
        }
        if ("SLEEP".equals(situation) || "TENDER".equals(situation) || "TENDER".equals(mood)) {
            return TimbreCode.SOFT;
        }
        if ("ANXIOUS".equals(situation) || "STRESSED".equals(mood)) {
            return TimbreCode.HEART;
        }
        return TimbreCode.HEART;
    }

    private static List<CurvePoint> curveFor(String situation, String mood, int bpm, int durationSeconds) {
        List<CurvePoint> points = new ArrayList<>();
        int steps = Math.max(4, durationSeconds / 8);
        boolean irregular = "ANXIOUS".equals(situation) || "STRESSED".equals(mood);
        boolean rise = "EXERCISE".equals(situation) || "ENERGETIC".equals(mood);
        boolean fall = "SLEEP".equals(situation) || "CALM".equals(mood);

        for (int i = 0; i <= steps; i++) {
            double t = durationSeconds * (i / (double) steps);
            double progress = i / (double) steps;
            int value = bpm;
            if (rise) {
                value = (int) Math.round(bpm * (0.86 + 0.14 * progress));
            } else if (fall) {
                value = (int) Math.round(bpm * (1.06 - 0.10 * progress));
            }
            if (irregular) {
                int jitter = (int) Math.round(6 * Math.sin(i * 1.7) + 3 * Math.cos(i * 0.9));
                value += jitter;
            }
            points.add(new CurvePoint(round(t), clamp(value, 40, 220)));
        }
        return points;
    }

    private static String defaultTitle(String situation, String mood) {
        return "Synth · " + situation.charAt(0) + situation.substring(1).toLowerCase(Locale.ROOT)
                + " / " + mood.charAt(0) + mood.substring(1).toLowerCase(Locale.ROOT);
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
