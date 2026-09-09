package com.heartbeatsend.heartbeat.dto;

public record ShareResponse(boolean shareEnabled, String shareToken, String shareUrl) {
}
