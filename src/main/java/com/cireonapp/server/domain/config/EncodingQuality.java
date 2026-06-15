package com.cireonapp.server.domain.config;

public enum EncodingQuality {
    LOSSLESS("lossless", "Lossless"),
    QUALITY("quality", "Quality"),
    BALANCED("balanced", "Balanced"),
    PERFORMANCE("performance", "Performance"),
    GARBAGE("garbage", "Garbage");

    private final String label;
    private final String name;

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    EncodingQuality(String label, String name) {
        this.label = label;
        this.name = name;
    }
}
