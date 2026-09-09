package com.heartbeatsend.common.config;

import com.heartbeatsend.HeartbeatSendApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HeartbeatProperties.class)
public class AppConfig {

    // Marker so IDEs and component scans stay next to HeartbeatSendApplication.
    @SuppressWarnings("unused")
    private static final Class<?> ROOT = HeartbeatSendApplication.class;
}
