package com.heartbeatsend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartbeatsend.auth.seed.DemoUserSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HeartbeatSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginCreateSynthShareAndPublicRead() throws Exception {
        String token = login();

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DemoUserSeeder.DEMO_USERNAME));

        MvcResult customResult = mockMvc.perform(post("/api/heartbeats/custom")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Studio pulse",
                                  "bpmNominal": 72,
                                  "timbreCode": "HEART",
                                  "durationSeconds": 30,
                                  "curve": [
                                    {"tSeconds": 0, "bpm": 70},
                                    {"tSeconds": 30, "bpm": 76}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originTag").value("CUSTOM"))
                .andExpect(jsonPath("$.originTagZh").value("自定义"))
                .andExpect(jsonPath("$.sensorOrigin").value(false))
                .andExpect(jsonPath("$.nonSensorLabel").exists())
                .andReturn();
        long customId = idOf(customResult);

        mockMvc.perform(post("/api/heartbeats/synth")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "situationCode": "REST",
                                  "moodCode": "CALM",
                                  "intensityCode": "LOW",
                                  "durationSeconds": 40,
                                  "note": "late evening"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originTag").value("GENERATED"))
                .andExpect(jsonPath("$.originTagZh").value("生成"))
                .andExpect(jsonPath("$.sensorOrigin").value(false))
                .andExpect(jsonPath("$.captureMode").value("RULE_SYNTH"))
                .andExpect(jsonPath("$.nonSensorLabel").value(org.hamcrest.Matchers.containsString("Non-sensor")));

        mockMvc.perform(post("/api/heartbeats/wearable-mock")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Watch mock", "durationSeconds": 20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originTag").value("MEASURED"))
                .andExpect(jsonPath("$.originTagZh").value("真测"))
                .andExpect(jsonPath("$.sensorOrigin").value(false))
                .andExpect(jsonPath("$.captureMode").value("WEARABLE_MOCK"));

        mockMvc.perform(get("/api/heartbeats").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordingId").exists());

        mockMvc.perform(get("/api/heartbeats").param("originTag", "CUSTOM")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originTag").value("CUSTOM"));

        MvcResult shareResult = mockMvc.perform(post("/api/heartbeats/" + customId + "/share")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareEnabled").value(true))
                .andReturn();
        String shareToken = objectMapper.readTree(shareResult.getResponse().getContentAsString())
                .path("shareToken").asText();

        mockMvc.perform(get("/api/public/heartbeats/" + shareToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Studio pulse · 72 BPM"))
                .andExpect(jsonPath("$.bpmNominal").value(72));

        mockMvc.perform(get("/api/wearable/status").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liveDeviceConnected").value(false));

        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());

        mockMvc.perform(delete("/api/heartbeats/" + customId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void customCreateAlignsFlatCurveAndTitleToNominalBpm() throws Exception {
        String token = login();

        MvcResult result = mockMvc.perform(post("/api/heartbeats/custom")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Studio pulse",
                                  "bpmNominal": 96,
                                  "timbreCode": "HEART",
                                  "durationSeconds": 45,
                                  "curve": [
                                    {"tSeconds": 0, "bpm": 72},
                                    {"tSeconds": 45, "bpm": 72}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bpmNominal").value(96))
                .andExpect(jsonPath("$.title").value("Studio pulse · 96 BPM"))
                .andExpect(jsonPath("$.curve[0].bpm").value(96))
                .andExpect(jsonPath("$.curve[1].bpm").value(96))
                .andReturn();
        long id = idOf(result);

        mockMvc.perform(get("/api/heartbeats").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordingId").value(id))
                .andExpect(jsonPath("$[0].title").value("Studio pulse · 96 BPM"))
                .andExpect(jsonPath("$[0].bpmNominal").value(96));
    }

    @Test
    void synthTitleIncludesBpm() throws Exception {
        String token = login();
        mockMvc.perform(post("/api/heartbeats/synth")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "situationCode": "REST",
                                  "moodCode": "CALM",
                                  "intensityCode": "LOW",
                                  "durationSeconds": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(org.hamcrest.Matchers.matchesPattern(".*\\d{2,3} BPM$")))
                .andExpect(jsonPath("$.bpmNominal").isNumber());
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightAllowsLocalhostOriginWithCredentials() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("POST")));
    }

    @Test
    void corsLoginAllowsLocalhostOrigin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Origin", "http://127.0.0.1:80")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(DemoUserSeeder.DEMO_USERNAME, DemoUserSeeder.DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:80"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(jsonPath("$.accessToken").isString());
    }

    @Test
    void corsPreflightRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(DemoUserSeeder.DEMO_USERNAME, DemoUserSeeder.DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("accessToken").asText();
    }

    private long idOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("recordingId").asLong();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
