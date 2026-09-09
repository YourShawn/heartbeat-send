package com.heartbeatsend.common.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds Spring {@code allowedOriginPatterns} from env CSV plus the origin of
 * {@code PUBLIC_BASE_URL}, so a non-localhost SPA can log in without committing a host IP.
 */
public final class CorsOriginPatterns {

    public static final String DEFAULT_CSV = "http://localhost:*,http://127.0.0.1:*";

    private CorsOriginPatterns() {
    }

    public static List<String> resolve(String allowedOriginPatternsCsv, String publicBaseUrl) {
        Set<String> patterns = new LinkedHashSet<>();
        addCsv(patterns, allowedOriginPatternsCsv);
        if (patterns.isEmpty()) {
            addCsv(patterns, DEFAULT_CSV);
        }
        addPublicBaseUrl(patterns, publicBaseUrl);
        return new ArrayList<>(patterns);
    }

    private static void addCsv(Set<String> patterns, String csv) {
        if (csv == null || csv.isBlank()) {
            return;
        }
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                patterns.add(trimmed);
            }
        }
    }

    private static void addPublicBaseUrl(Set<String> patterns, String publicBaseUrl) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(publicBaseUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return;
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            String hostForOrigin = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
            int port = uri.getPort();
            String origin = scheme + "://" + hostForOrigin + (port != -1 ? ":" + port : "");
            patterns.add(origin);
            patterns.add(scheme + "://" + hostForOrigin + ":*");
        } catch (IllegalArgumentException ignored) {
            // Malformed PUBLIC_BASE_URL is not a CORS pattern.
        }
    }
}
