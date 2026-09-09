package com.heartbeatsend.heartbeat.service;

import com.heartbeatsend.heartbeat.domain.CurvePoint;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeps stored title BPM, nominal BPM, and curve BPM on the same value
 * so library cards, player chrome, and playback do not disagree.
 */
public final class BpmAlignment {

    public static final int MIN_BPM = 40;
    public static final int MAX_BPM = 220;

    private static final Pattern TITLE_BPM = Pattern.compile("(?i)\\b\\d{2,3}\\s*BPM\\b");

    private BpmAlignment() {
    }

    public static int clampBpm(int bpm) {
        return Math.min(MAX_BPM, Math.max(MIN_BPM, bpm));
    }

    /**
     * Shift (or fill) the curve so its representative BPM equals {@code bpmNominal}.
     * A flat curve at 72 with nominal 96 becomes a flat curve at 96.
     */
    public static List<CurvePoint> alignCurveToNominal(List<CurvePoint> curve, int bpmNominal, int durationSeconds) {
        int nominal = clampBpm(bpmNominal);
        int duration = Math.max(1, durationSeconds);
        if (curve == null || curve.isEmpty()) {
            return List.of(new CurvePoint(0, nominal), new CurvePoint(duration, nominal));
        }
        int representative = representativeBpm(curve);
        int delta = nominal - representative;
        List<CurvePoint> aligned = new ArrayList<>(curve.size());
        for (CurvePoint point : curve) {
            double t = Math.min(duration, Math.max(0, point.tSeconds()));
            aligned.add(new CurvePoint(t, clampBpm(point.bpm() + delta)));
        }
        return List.copyOf(aligned);
    }

    /**
     * Replace an existing {@code N BPM} token, or append {@code · N BPM}.
     */
    public static String alignTitleBpm(String title, int bpmNominal) {
        int nominal = clampBpm(bpmNominal);
        String token = nominal + " BPM";
        String raw = title == null ? "" : title.trim();
        if (raw.isEmpty()) {
            return token;
        }
        Matcher matcher = TITLE_BPM.matcher(raw);
        if (matcher.find()) {
            return TITLE_BPM.matcher(raw).replaceAll(Matcher.quoteReplacement(token));
        }
        return raw + " · " + token;
    }

    private static int representativeBpm(List<CurvePoint> curve) {
        int first = curve.get(0).bpm();
        boolean flat = true;
        long sum = 0;
        for (CurvePoint point : curve) {
            sum += point.bpm();
            if (point.bpm() != first) {
                flat = false;
            }
        }
        if (flat) {
            return first;
        }
        return (int) Math.round(sum / (double) curve.size());
    }
}
