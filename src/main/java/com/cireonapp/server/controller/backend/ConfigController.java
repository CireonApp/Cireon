package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.dto.SuccessResponseDto;
import com.cireonapp.server.dto.UpdateConfigRequestDto;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Config API", description = "Config related endpoints")
@RestController
@RequestMapping("/api/config")
public class ConfigController {


    @Operation(
            summary = "Get config.",
            description = "Get the current config."
    )
    @GetMapping("/get")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved config",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Config.class
                            )
                    )
            )
    })
    public static ResponseEntity<?> get(HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        boolean isUserAdmin = user.get().isAdministrator();

        if (isUserAdmin)
            return ResponseEntity.ok(ConfigManager.get());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - An error occurred while processing your request.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated config",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SuccessResponseDto.class
                            )
                    )
            )
    })
    @Operation(
            summary = "Update config.",
            description = "Update the config. Might require restarting the app for some changes to apply."
    )
    @PutMapping("/update")
    public static ResponseEntity<?> update(@Valid @RequestBody UpdateConfigRequestDto newConfig, HttpServletRequest request) {

        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);


        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        boolean isAdmin = user.get().isAdministrator();


        if (!isAdmin)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);


        Config config = ConfigManager.get();
        if (newConfig.maxUsers != null) config.setMaxUsers(newConfig.maxUsers);
        if (newConfig.port != null) config.setPort(newConfig.port);
        if (newConfig.allowUserCreation != null) config.setAllowUserCreation(newConfig.allowUserCreation);
        boolean result = ConfigManager.update(config);

        if (result) return ResponseEntity.ok(new SuccessResponseDto("Updated config successfully!"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
    }


}
