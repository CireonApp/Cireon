package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Login Response",
        description = "Response DTO for login responses. Contains the display name of the user and their permissions."
)
public class LoginResponseDto extends ResponseDto {

    @Schema(description = "Display name of the user")
    public String displayName;
    @Schema(description = "Whether the user has admin permissions or not")
    public boolean admin;
    @Schema(description = "Last interaction date of the user")
    public String lastUse;
    @Schema(description = "Creation date of the user")
    public String created;


    public LoginResponseDto(User user) {
        super();
        convertFromUser(user);
    }

    private void convertFromUser(User user) {
        this.displayName = user.getDisplayName();
        this.admin = user.isAdministrator();
        this.lastUse = user.getLastUseDate();
        this.created = user.getCreationDate();
    }
}