package com.cireonapp.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(
        name = "New User Request",
        description = "Request DTO for creating a new user. Contains username, password, and display name."
)
public class NewUserRequestDto {

    @NotNull(message="Username is required")
    @NotBlank(message="Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores.")
    @Size(min = 6,max = 18, message = "Username must be between 6 and 18 characters.")
    @Schema(defaultValue = "john_doe",description = "Username of the new user")
    public String username;

    @NotNull(message="Password is required")
    @NotBlank(message="Password is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Password can only contain letters and numbers.")
    @Size(min = 8,max=24, message = "Password must be at least 8 characters long.")
    @Schema(description = "Password of the new user", defaultValue = "johndoetheking123")
    public String password;

    @NotNull(message="Display name is required")
    @NotBlank(message="Display name is required")
    @Schema(defaultValue = "John Doe", description = "Display name of the new user")
    @Size(min = 2, max= 20, message = "Display name must be between 2 and 20 characters.")
    public String displayName;
}
