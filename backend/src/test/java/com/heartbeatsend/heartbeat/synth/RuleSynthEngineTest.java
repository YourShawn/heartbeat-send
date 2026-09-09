package com.heartbeatsend.heartbeat.synth;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartbeatsend.heartbeat.domain.CaptureMode;
import com.heartbeatsend.heartbeat.domain.TimbreCode;
import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import org.junit.jupiter.api.Test;

class RuleSynthEngineTest {

    private final RuleSynthEngine engine = new RuleSynthEngine();

    @Test
    void restCalmStaysSlowAndLabeledNonSensor() {
        SynthPlan plan = engine.plan(new CreateSynthRequest(
                null, "REST", "CALM", "LOW", 30, "evening"
        ));
        assertThat(plan.captureMode()).isEqualTo(CaptureMode.RULE_SYNTH);
        assertThat(plan.synthModel()).isNull();
        assertThat(plan.nonSensorLabel()).contains("Non-sensor");
        assertThat(plan.bpmNominal()).isBetween(48, 80);
        assertThat(plan.timbreCode()).isIn(TimbreCode.HEART, TimbreCode.SOFT);
        assertThat(plan.curve()).hasSizeGreaterThan(2);
    }

    @Test
    void exerciseEnergeticRaisesBpm() {
        SynthPlan calm = engine.plan(new CreateSynthRequest(null, "REST", "CALM", "LOW", 20, null));
        SynthPlan hard = engine.plan(new CreateSynthRequest(null, "EXERCISE", "ENERGETIC", "HIGH", 20, null));
        assertThat(hard.bpmNominal()).isGreaterThan(calm.bpmNominal());
        assertThat(hard.timbreCode()).isEqualTo(TimbreCode.DRUM);
    }
}
