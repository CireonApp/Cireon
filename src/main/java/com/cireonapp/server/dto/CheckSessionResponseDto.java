package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserSettings;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Check Session Response",
        description = "Response DTO for checking the session. Contains the username, display name, permissions and settings of the user."
)
public class CheckSessionResponseDto extends ResponseDto {
    @Schema(description = "Display name of the user that is logged in")
    public String displayName;
    @Schema(description = "Username of the user that is logged in")
    public String username;
    @Schema(description = "Whether the user that is logged in is an admin or not")
    public boolean admin;
    @Schema(description = "Settings of the user that is logged in")
    public UserSettings settings;

    public CheckSessionResponseDto(String username, String displayName, boolean admin, UserSettings settings) {
        super();
        this.username = username;
        this.displayName = displayName;
        this.admin = admin;
        this.settings = settings;
    }
}