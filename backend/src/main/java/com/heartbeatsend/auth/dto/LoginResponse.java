package com.heartbeatsend.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        UserProfileResponse profile
) {
}
