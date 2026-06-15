package com.cireonapp.server.domain.config;

import io.swagger.v3.oas.annotations.media.Schema;
import org.dizitart.no2.repository.annotations.Entity;


@Entity(value = "config")
@Schema(hidden = true)
public class Config {
    public static final Config DEFAULT = getDefault();

    @Schema(defaultValue = "14567", description = "The port the server is listening to")
    private Integer port = 14567;
    @Schema(defaultValue = "8", description = "Max amount of users that can be created. Admins can create without limitations")
    private Integer maxUsers = 8;
    @Schema(defaultValue = "true", description = "Whether users are allowed to be created at all. If false, only admins can create users")
    private Boolean allowUserCreation = true;
    @Schema(defaultValue = "false", description = "Whether users are allowed to be created at all. If false, only admins can create users")
    private Boolean firstTimeSetupComplete = false;
    @Schema(defaultValue = "true", description = "Whether hardware acceleration is enabled. If false, the server will use software rendering, which may be slower but more compatible with older hardware.")
    private Boolean hardwareAcceleration = true;
    private HardwareEncoders encoder;
    private EncodingQuality encodingQuality;

    private static Config getDefault() {
        Config config = new Config();
        config.setPort(14567);
        config.setMaxUsers(8);
        config.setAllowUserCreation(true);
        config.setFirstTimeSetupComplete(false);
        config.setHardwareAcceleration(true);
        config.setEncodingQuality(EncodingQuality.BALANCED);
        return config;
    }

    public Config() {
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Boolean isAllowUserCreation() {
        return allowUserCreation;
    }

    public void setAllowUserCreation(Boolean allowUserCreation) {
        this.allowUserCreation = allowUserCreation;
    }

    public Boolean isFirstTimeSetupComplete() {
        return firstTimeSetupComplete;
    }

    public void setFirstTimeSetupComplete(Boolean firstTimeSetupComplete) {
        this.firstTimeSetupComplete = firstTimeSetupComplete;
    }

    public Boolean getHardwareAcceleration() {
        return hardwareAcceleration;
    }

    public void setHardwareAcceleration(Boolean hardwareAcceleration) {
        this.hardwareAcceleration = hardwareAcceleration;
    }

    public EncodingQuality getEncodingQuality() {
        return encodingQuality;
    }

    public void setEncodingQuality(EncodingQuality encodingQuality) {
        this.encodingQuality = encodingQuality;
    }

    public HardwareEncoders getEncoder() {
        return encoder;
    }

    public void setEncoder(HardwareEncoders encoder) {
        this.encoder = encoder;
    }

    public void merge(Config source) {
        if (source.getPort() != null)
            this.port = source.getPort();
        if (source.getMaxUsers() != null)
            this.maxUsers = source.getMaxUsers();
        if (source.isAllowUserCreation() != null)
            this.allowUserCreation = source.isAllowUserCreation();
        if (source.isFirstTimeSetupComplete() != null)
            this.firstTimeSetupComplete = source.isFirstTimeSetupComplete();
        if (source.getHardwareAcceleration() != null) {
            this.hardwareAcceleration = source.getHardwareAcceleration();
        }
        if (source.getEncodingQuality() != null) {
            this.encodingQuality = source.getEncodingQuality();
        }
        if(source.getEncoder() != null){
            this.encoder = source.getEncoder();
        }
    }


}
