package com.cireonapp.server.dto;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.EncodingQuality;
import com.cireonapp.server.domain.config.HardwareEncoders;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


@Schema(
        name = "Update Config Request",
        description = "Request DTO for updating server configuration.")
public class UpdateConfigRequestDto {
    @Min(value = 0, message = "port must be above 0!")
    @Max(value = 65535, message = "port must be above 65535!")
    @Schema(defaultValue = "14567", description = "REQUIRES RESTART! the port the server is listening to")
    public Integer port;

    @Min(value = 0, message = "maxUsers must be above 0!")
    @Schema(defaultValue = "8", description = "Max amount of users that can be created. Admins can create without limitations")
    public Integer maxUsers;

    @Schema(defaultValue = "true", description = "Whether users are allowed to be created at all. If false, only admins can create users")
    public Boolean allowUserCreation;

    @Schema(defaultValue = "false", description = "Whether the first time setup has been completed.")
    public Boolean firstTimeSetupComplete;

    @Schema(defaultValue = "true", description = "Whether hardware acceleration is enabled. If false, the server will use software rendering, which may be slower but more compatible with older hardware.")
    public Boolean hardwareAcceleration;

    @Schema(defaultValue = "AUTO")
    public HardwareEncoders encoder;

    @Schema(defaultValue = "BALANCED")
    public EncodingQuality encodingQuality;

    public Config toConfig(){
        Config config = new Config();
        config.setPort(this.port);
        config.setMaxUsers(this.maxUsers);
        config.setAllowUserCreation(this.allowUserCreation);
        config.setFirstTimeSetupComplete(this.firstTimeSetupComplete);
        config.setHardwareAcceleration(this.hardwareAcceleration);
        config.setEncoder(this.encoder);
        config.setEncodingQuality(this.encodingQuality);
        return config;
    }
}
