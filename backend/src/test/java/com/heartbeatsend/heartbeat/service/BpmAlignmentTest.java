package com.heartbeatsend.heartbeat.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartbeatsend.heartbeat.domain.CurvePoint;
import java.util.List;
import org.junit.jupiter.api.Test;

class BpmAlignmentTest {

    @Test
    void flatCurveAt72AlignsToNominal96() {
        List<CurvePoint> aligned = BpmAlignment.alignCurveToNominal(
                List.of(new CurvePoint(0, 72), new CurvePoint(45, 72)),
                96,
                45
        );
        assertThat(aligned).containsExactly(new CurvePoint(0, 96), new CurvePoint(45, 96));
    }

    @Test
    void emptyCurveBecomesFlatNominal() {
        List<CurvePoint> aligned = BpmAlignment.alignCurveToNominal(List.of(), 88, 30);
        assertThat(aligned).containsExactly(new CurvePoint(0, 88), new CurvePoint(30, 88));
    }

    @Test
    void variedCurveShiftsSoMeanMatchesNominal() {
        List<CurvePoint> aligned = BpmAlignment.alignCurveToNominal(
                List.of(new CurvePoint(0, 70), new CurvePoint(30, 76)),
                72,
                30
        );
        double mean = aligned.stream().mapToInt(CurvePoint::bpm).average().orElseThrow();
        assertThat(mean).isEqualTo(72.0);
        assertThat(aligned.get(1).bpm() - aligned.get(0).bpm()).isEqualTo(6);
    }

    @Test
    void clampCurveTimesToDuration() {
        List<CurvePoint> aligned = BpmAlignment.alignCurveToNominal(
                List.of(new CurvePoint(0, 80), new CurvePoint(90, 80)),
                80,
                40
        );
        assertThat(aligned.get(1).tSeconds()).isEqualTo(40);
    }

    @Test
    void titleReplacesExistingBpmToken() {
        assertThat(BpmAlignment.alignTitleBpm("My pulse · 72 BPM", 96))
                .isEqualTo("My pulse · 96 BPM");
    }

    @Test
    void titleAppendsBpmWhenMissing() {
        assertThat(BpmAlignment.alignTitleBpm("Studio pulse", 96))
                .isEqualTo("Studio pulse · 96 BPM");
    }

    @Test
    void blankTitleBecomesBpmOnly() {
        assertThat(BpmAlignment.alignTitleBpm("  ", 64)).isEqualTo("64 BPM");
    }
}
