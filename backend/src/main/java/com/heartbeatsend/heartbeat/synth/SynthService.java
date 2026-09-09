package com.heartbeatsend.heartbeat.synth;

import com.heartbeatsend.heartbeat.dto.CreateSynthRequest;
import org.springframework.stereotype.Service;

@Service
public class SynthService {

    private final RuleSynthEngine ruleSynthEngine;
    private final OpenAiSynthClient openAiSynthClient;

    public SynthService(RuleSynthEngine ruleSynthEngine, OpenAiSynthClient openAiSynthClient) {
        this.ruleSynthEngine = ruleSynthEngine;
        this.openAiSynthClient = openAiSynthClient;
    }

    public SynthPlan synthesize(CreateSynthRequest request) {
        return openAiSynthClient.tryPlan(request).orElseGet(() -> ruleSynthEngine.plan(request));
    }
}
