package com.heartbeatsend.heartbeat.dto;

public record WearableStatusResponse(
        boolean liveDeviceConnected,
        String providerName,
        String capturePath,
        String messageZh,
        String messageEn
) {
}
