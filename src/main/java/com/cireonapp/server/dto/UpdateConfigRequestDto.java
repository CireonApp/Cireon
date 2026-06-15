package com.cireonapp.server.dto;

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
}
