package com.heartbeatsend.heartbeat.controller;

import com.heartbeatsend.heartbeat.dto.WearableStatusResponse;
import com.heartbeatsend.heartbeat.wearable.WearableHeartRateProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wearable")
@Tag(name = "Wearable")
public class WearableController {

    private final WearableHeartRateProvider wearableHeartRateProvider;

    public WearableController(WearableHeartRateProvider wearableHeartRateProvider) {
        this.wearableHeartRateProvider = wearableHeartRateProvider;
    }

    @GetMapping("/status")
    @Operation(summary = "Live-device status. Always disconnected until a real provider is plugged in.")
    public WearableStatusResponse status() {
        boolean live = wearableHeartRateProvider.isLiveDeviceConnected();
        return new WearableStatusResponse(
                live,
                wearableHeartRateProvider.providerName(),
                live ? "WEARABLE_LIVE" : "WEARABLE_MOCK",
                live
                        ? "已连接真实设备。读数仍不能用于诊疗。"
                        : "尚未接入真实可穿戴设备。当前仅提供模拟会话。",
                live
                        ? "A live device is connected. Readings are still not for diagnosis."
                        : "No live wearable is connected. Only a mock session is available."
        );
    }
}
