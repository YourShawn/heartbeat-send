package com.heartbeatsend.common.security;

import com.heartbeatsend.common.config.HeartbeatProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final HeartbeatProperties properties;

    public JwtService(HeartbeatProperties properties) {
        this.properties = properties;
    }

    public String issueToken(long userId, String username) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(properties.getJwt().getExpirationMs());
        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey())
                .compact();
    }

    public long expirationMs() {
        return properties.getJwt().getExpirationMs();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] bytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
