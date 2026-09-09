package com.heartbeatsend.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

class CorsOriginPatternsTest {

    @Test
    void defaultsWhenCsvBlank() {
        assertThat(CorsOriginPatterns.resolve("  ", null))
                .containsExactly("http://localhost:*", "http://127.0.0.1:*");
    }

    @Test
    void parsesCommaSeparatedPatterns() {
        List<String> resolved = CorsOriginPatterns.resolve(
                "http://localhost:*, http://127.0.0.1:* , https://app.example:*",
                null
        );
        assertThat(resolved).containsExactly(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://app.example:*"
        );
    }

    @Test
    void addsPublicBaseUrlOriginAndHostPattern() {
        List<String> resolved = CorsOriginPatterns.resolve(
                "http://localhost:*",
                "http://192.0.2.10:8080/share"
        );
        assertThat(resolved).containsExactly(
                "http://localhost:*",
                "http://192.0.2.10:8080",
                "http://192.0.2.10:*"
        );
    }

    @Test
    void publicBaseUrlWithoutPortStillAddsHostPattern() {
        List<String> resolved = CorsOriginPatterns.resolve(
                CorsOriginPatterns.DEFAULT_CSV,
                "http://deploy.example"
        );
        assertThat(resolved).contains(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://deploy.example",
                "http://deploy.example:*"
        );
    }

    @Test
    void ignoresMalformedPublicBaseUrl() {
        assertThat(CorsOriginPatterns.resolve("http://localhost:*", "not a uri ://"))
                .containsExactly("http://localhost:*");
    }

    @Test
    void springCorsAllowsPublicBaseUrlHostAnyPortWithCredentials() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(CorsOriginPatterns.resolve(
                "http://localhost:*",
                "http://192.0.2.10"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        assertThat(config.checkOrigin("http://192.0.2.10")).isEqualTo("http://192.0.2.10");
        assertThat(config.checkOrigin("http://192.0.2.10:80")).isEqualTo("http://192.0.2.10:80");
        assertThat(config.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(config.checkOrigin("http://evil.example")).isNull();
    }
}
