package com.cireonapp.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Schema(
        name = "Login Request",
        description = "Request DTO for user login. Contains username and password fields with validation constraints.")
public class LoginRequestDto {
    @NotNull(message = "Username is required")
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores.")
    @Size(min = 6, max = 18, message = "Username must be between 6 and 18 characters.")
    @Schema(description = "Username of the user you want to log into", defaultValue = "john_doe")
    public String username;

    @NotNull(message="Password is required")
    @NotBlank(message="Password is required")
    @NotNull(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Password can only contain letters and numbers.")
    @Size(min = 8,max=24, message = "Password must be at least 8 characters long.")
    @Schema(description = "Password of the user you want to log into", defaultValue = "johndoetheking123")
    @Size(min = 8, max = 24, message = "Password must be at least 8 characters long.")
    public String password;
}
