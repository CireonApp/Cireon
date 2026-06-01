package com.cireonapp.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminLoginToUserDto {
    @NotNull(message = "Username is required")
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores.")
    @Size(min = 6, max = 18, message = "Username must be between 6 and 18 characters.")
    @Schema(description = "Username of the user you want to log into", defaultValue = "john_doe")
    public String username;
}
