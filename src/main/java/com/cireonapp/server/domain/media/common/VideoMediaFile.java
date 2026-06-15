package com.cireonapp.server.domain.media.common;

import org.dizitart.no2.repository.annotations.Id;

import java.nio.file.Path;

public class VideoMediaFile {
    @Id
    private String hash;
    private Path filePath;
    private String videoCodec;
    private String audioCodec;
    private Integer height;
    private Integer width;
    private Double duration;
    private Double fps;

    public VideoMediaFile(String hash, Path filePath, String videoCodec, String audioCodec, Integer height, Integer width, Double duration, Double fps) {
        this.hash = hash;
        this.filePath = filePath;
        this.videoCodec = videoCodec;
        this.audioCodec = audioCodec;
        this.height = height;
        this.width = width;
        this.duration = duration;
        this.fps = fps;
    }

    public VideoMediaFile() {
    }

    public void merge(VideoMediaFile source) {
        if (source.filePath != null){
            this.filePath = source.filePath;
            // Don't update hash - it's content-based and shouldn't change
        }
        if (source.videoCodec != null) this.videoCodec = source.videoCodec;
        if (source.audioCodec != null) this.audioCodec = source.audioCodec;
        if (source.height != null) this.height = source.height;
        if (source.width != null) this.width = source.width;
        if (source.duration != null) this.duration = source.duration;
        if (source.fps != null) this.fps = source.fps;
    }

    public String getHash() {
        return this.hash;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
        // Don't recalculate hash here - hash should be based on content, not path
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getFps() {
        return fps;
    }

    public void setFps(Double fps) {
        this.fps = fps;
    }
}
