package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserPermissions;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        name = "Login Response",
        description = "Response DTO for login responses. Contains the display name of the user and their permissions."
)
public class LoginResponseDto extends ResponseDto {

    @Schema(description = "Display name of the user that logged in")
    public String displayName;
    @Schema(description = "Permissions of the user that logged in")
    public Set<UserPermissions> permissions;

    public LoginResponseDto(String displayName, Set<UserPermissions> permissions) {
        super();
        this.displayName = displayName;
        this.permissions = permissions;
    }
}