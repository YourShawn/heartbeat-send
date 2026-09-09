package com.heartbeatsend.heartbeat.domain;

public enum OriginTag {
    CUSTOM,
    GENERATED,
    MEASURED;

    public String zhLabel() {
        return switch (this) {
            case CUSTOM -> "自定义";
            case GENERATED -> "生成";
            case MEASURED -> "真测";
        };
    }

    public String enLabel() {
        return switch (this) {
            case CUSTOM -> "Custom";
            case GENERATED -> "Generated";
            case MEASURED -> "Measured";
        };
    }
}
