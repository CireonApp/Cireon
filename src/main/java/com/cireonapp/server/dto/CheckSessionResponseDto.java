package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.domain.user.UserSettings;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(
        name = "Check Session Response",
        description = "Response DTO for checking the session. Contains the username, display name, permissions and settings of the user."
)
public class CheckSessionResponseDto extends ResponseDto {
    @Schema(description = "Display name of the user that is logged in")
    public String displayName;
    @Schema(description = "Username of the user that is logged in")
    public String username;
    @Schema(description = "Permissions of the user that is logged in")
    public Set<UserPermissions> permissions;
    @Schema(description = "Settings of the user that is logged in")
    public UserSettings settings;

    public CheckSessionResponseDto(String username, String displayName, Set<UserPermissions> permissions, UserSettings settings) {
        super();
        this.username = username;
        this.displayName = displayName;
        this.permissions = permissions;
        this.settings = settings;
    }
}