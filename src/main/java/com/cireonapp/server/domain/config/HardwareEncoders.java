package com.cireonapp.server.domain.config;

public enum HardwareEncoders {
    NVIDIA_NVENC("h264_nvenc", "Nvidia NVENC (H264)"),
    INTEL_QUICKSYNC("h264_qsv", "Intel Quick Sync (H264)"),
    AMD_VCE("h264_amf", "AMD VCE (H264)"),
    LIBX264("libx264", "LibX264 (H264) - Software!"),
    AUTO("auto","Automatically Pick");


    private final String label;
    private final String name;

    public String getLabel() {
        return this.label;
    }
    public String getName() {
        return this.name;
    }

    public static HardwareEncoders getByLabel(String label) {
        for (HardwareEncoders encoder : HardwareEncoders.values()) {
            if (encoder.getLabel().equals(label)) {
                return encoder;
            }
        }
        return null; // or throw an exception if you prefer
    }


    HardwareEncoders(String label, String name) {
        this.label = label;
        this.name = name;
    }
}
