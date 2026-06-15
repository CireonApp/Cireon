package com.cireonapp.server.service;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.config.EncodingQuality;
import com.cireonapp.server.domain.config.HardwareEncoders;
import com.cireonapp.server.domain.media.common.MediaInfoByFFmpeg;
import org.apache.commons.lang3.SystemUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class FFmpegServices {

    /**
     * if hardware acceleration is disabled will always return libx264.
     *
     * @return
     */
    public static HardwareEncoders getEncoder() {
        if (ConfigManager.get().getEncoder() == null) setEncoder(getBestAvailableHardwareCodec(), false);
        if (ConfigManager.get().getHardwareAcceleration()) return ConfigManager.get().getEncoder();
        return HardwareEncoders.LIBX264;
    }

    public static void setEncoder(HardwareEncoders cachedEncoder, boolean updateConfig) {
        if (updateConfig) {
            Config config = new Config();
            config.setEncoder(cachedEncoder);
            ConfigManager.update(config);
        }
    }

    /**
     * Get only hardware codecs
     *
     * @return
     */
    public static HardwareEncoders getBestAvailableHardwareCodec() {
        // Define the priority order
        HardwareEncoders[] priorityList = {HardwareEncoders.NVIDIA_NVENC, HardwareEncoders.INTEL_QUICKSYNC, HardwareEncoders.AMD_VCE};

        try {
            Process p = new ProcessBuilder("ffmpeg", "-encoders").start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            // Loop through our list of hardware codecs
            for (HardwareEncoders codec : priorityList) {
                String label = codec.getLabel();
                // Only attempt if the encoder is present AND passes the dry-run test
                if (output.contains(label)) {
                    if (canInitializeCodec(label)) {
                        setEncoder(codec, true);
                        return codec;
                    }
                }
            }
        } catch (Exception ignored) {
            setEncoder(HardwareEncoders.LIBX264, true);
            return HardwareEncoders.LIBX264;
        }

        setEncoder(HardwareEncoders.LIBX264, true);
        return HardwareEncoders.LIBX264;
    }

    public static boolean canInitializeCodec(String codec) {
        try {

            // Run a dummy transcode of 1 frame to see if it crashes
            Process p = new ProcessBuilder("ffmpeg", "-f", "lavfi", "-i", "testsrc=duration=0.1:size=320x240:rate=1", "-vcodec", codec, "-f", "null", "-").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }


    public static MediaInfoByFFmpeg getVideoInfo(String videoPath) {
        ServerApplication.LOGGER.info(videoPath);
        if (videoPath == null || videoPath.isBlank()) return null;

        try {
            // -print_format json makes parsing trivial
            ProcessBuilder pb = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration:stream=codec_type,codec_name,r_frame_rate,width,height", "-of", "json", videoPath);

            Process process = pb.start();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(process.getInputStream());

            MediaInfoByFFmpeg info = new MediaInfoByFFmpeg();
            JsonNode durationNode = root.path("format").path("duration");
            if (!durationNode.isMissingNode()) info.setDuration(durationNode.asDouble());

            for (JsonNode stream : root.path("streams")) {
                String type = stream.path("codec_type").asString();

                if ("video".equals(type)) {
                    JsonNode codecNode = stream.path("codec_name");
                    if (!codecNode.isMissingNode()) info.setVideoCodec(codecNode.asString());

                    JsonNode widthNode = stream.path("width");
                    if (!widthNode.isMissingNode()) info.setWidth(widthNode.asInt());

                    JsonNode heightNode = stream.path("height");
                    if (!heightNode.isMissingNode()) info.setHeight(heightNode.asInt());


                    // Parse FPS
                    JsonNode fps = stream.path("r_frame_rate");
                    if (!fps.isMissingNode() && fps.asString().contains("/")) {
                        String[] parts = fps.asString().split("/");
                        info.setFps(Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]));
                    }
                } else if ("audio".equals(type)) {
                    JsonNode codecNode = stream.path("codec_name");
                    if (!codecNode.isMissingNode()) info.setAudioCodec(codecNode.asString());
                }
            }
            return process.waitFor() == 0 ? info : null;
        } catch (Exception e) {
            return null;
        }
    }


    public static boolean checkFFmpegInstallation() {
        try {
            ProcessBuilder ffmpegPb = new ProcessBuilder("ffprobe", "-version");
            ffmpegPb.redirectErrorStream(true);
            Process ffmpegProcess = ffmpegPb.start();

            ProcessBuilder ffprobePb = new ProcessBuilder("ffprobe", "-version");
            ffprobePb.redirectErrorStream(true);
            Process ffprobeProcess = ffprobePb.start();

            ffmpegProcess.getInputStream().transferTo(OutputStream.nullOutputStream());
            ffprobeProcess.getInputStream().transferTo(OutputStream.nullOutputStream());

            if (ffmpegProcess.waitFor() != 0) return false;
            if (ffprobeProcess.waitFor() != 0) return false;
        } catch (IOException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    public static boolean downloadFFmpeg() {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.out.print("\n[INFO] Downloading for Windows...");
            return FFmpegDownloadService.downloadForWindows();
        }
        System.out.print("\n[ERROR] Could not find an automated installation method for this OS. Please install FFmpeg manually and ensure it's in your system PATH.");
        return false;
    }


    /**
     * Get the preset for the currently selected codec and encoding quality.
     */
    public static String resolvePresetForCodec() {
        Config config = ConfigManager.get();
        EncodingQuality encodingQuality = config.getEncodingQuality();

        if (encodingQuality == null) encodingQuality = Config.DEFAULT.getEncodingQuality();

        HardwareEncoders codec = getEncoder();


        return resolvePresetForCodec(codec, encodingQuality);
    }

    /**
     * Get a preset for the selected codec and encoding quality.
     */
    public static String resolvePresetForCodec(HardwareEncoders codec, EncodingQuality encodingQuality) {

        return switch (codec) {
            case NVIDIA_NVENC -> switch (encodingQuality) {
                case LOSSLESS -> "p7";
                case QUALITY -> "p6";
                case BALANCED -> "p4";
                case PERFORMANCE -> "p2";
                case GARBAGE -> "p1";
            };
            case INTEL_QUICKSYNC -> switch (encodingQuality) {
                case LOSSLESS -> "veryslow";
                case QUALITY -> "slower";
                case BALANCED -> "medium";
                case PERFORMANCE -> "fast";
                case GARBAGE -> "veryfast";
            };
            case AMD_VCE -> switch (encodingQuality) {
                case LOSSLESS -> "quality";
                case QUALITY -> "quality";
                case BALANCED -> "balanced";
                case PERFORMANCE -> "speed";
                case GARBAGE -> "speed";
            };
            case LIBX264 -> switch (encodingQuality) {
                case LOSSLESS -> "veryslow";
                case QUALITY -> "slower";
                case BALANCED -> "medium";
                case PERFORMANCE -> "veryfast";
                case GARBAGE -> "ultrafast";
            };
            case AUTO -> null;
        };
    }

    public static List<String> resolveRateControlParameter(HardwareEncoders codec, EncodingQuality encodingQuality) {

        return switch (codec) {

            case NVIDIA_NVENC -> switch (encodingQuality) {
                case LOSSLESS -> List.of("-rc", "constqp", "-qp", "0");
                case QUALITY -> List.of("-rc", "vbr_hq", "-cq", "18");
                case BALANCED -> List.of("-rc", "vbr_hq", "-cq", "23");
                case PERFORMANCE -> List.of("-rc", "vbr", "-cq", "28");
                case GARBAGE -> List.of("-rc", "vbr", "-cq", "32");
            };

            case INTEL_QUICKSYNC -> switch (encodingQuality) {
                case LOSSLESS -> List.of("-rc", "constqp", "-global_quality", "1");
                case QUALITY -> List.of("-rc", "icq", "-global_quality", "18");
                case BALANCED -> List.of("-rc", "icq", "-global_quality", "23");
                case PERFORMANCE -> List.of("-rc", "icq", "-global_quality", "28");
                case GARBAGE -> List.of("-rc", "icq", "-global_quality", "32");
            };

            case AMD_VCE -> switch (encodingQuality) {
                case LOSSLESS -> List.of("-rc", "cqp", "-qp_i", "0");
                case QUALITY -> List.of("-rc", "vbr_peak", "-qp_i", "18");
                case BALANCED -> List.of("-rc", "vbr_peak", "-qp_i", "23");
                case PERFORMANCE -> List.of("-rc", "vbr_peak", "-qp_i", "28");
                case GARBAGE -> List.of("-rc", "vbr_latency", "-qp_i", "32");
            };

            case LIBX264 -> switch (encodingQuality) {
                case LOSSLESS -> List.of("-crf", "0", "-x264-params", "lossless=1");
                case QUALITY -> List.of("-crf", "18");
                case BALANCED -> List.of("-crf", "23");
                case PERFORMANCE -> List.of("-crf", "28");
                case GARBAGE -> List.of("-crf", "32", "-tune", "zerolatency");
            };

            case AUTO -> List.of();
        };

    }


    public static List<String> resolveRateControlParameter() {
        Config config = ConfigManager.get();
        EncodingQuality encodingQuality = config.getEncodingQuality();
        if (encodingQuality == null) encodingQuality = Config.DEFAULT.getEncodingQuality();

        return resolveRateControlParameter(getEncoder(), encodingQuality);
    }
}
