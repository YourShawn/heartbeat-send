package com.heartbeatsend.auth.dto;

public record UserProfileResponse(
        long userId,
        String username,
        String displayName
) {
}
