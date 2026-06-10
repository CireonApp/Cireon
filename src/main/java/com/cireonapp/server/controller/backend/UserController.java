package com.cireonapp.server.controller.backend;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.*;
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

@Tag(name = "User API", description = "User related endpoints")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Operation(
            summary = "Create a user.",
            description = "Create a new user."
    )
    @PostMapping(value = "/create")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully created a user.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = LoginResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Maximum number of users reached or insufficient permissions.",
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
                    responseCode = "400",
                    description = "Bad Request - Invalid input data.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserRequestDto user, HttpServletRequest request) {


        int maxUsers = ConfigManager.get().getMaxUsers();
        long userCount = UserManager.getAll().size();

        Optional<User> authenticatedUser = CookieHelper.getUserFromSessionCookie(request);

        boolean isAdmin = authenticatedUser.isPresent() && authenticatedUser.get().isAdministrator();

        if (!isAdmin) {
            if (!ConfigManager.get().isAllowUserCreation())
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDto("Only administrators can create new users."));
            if (userCount >= maxUsers)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDto("Maximum number of users reached."));
        }
        User newUser = new User(user.username, user.password, user.displayName, user.allowAdultContent);

        boolean success = UserManager.create(newUser);
        if (success)
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDto(newUser));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);

    }

    @Operation(
            summary = "Delete a user.",
            description = "Delete a user. You can delete your own user when authenticated or if you have the right permissions, you can delete any user. Leave username param empty to delete your use."
    )
    @DeleteMapping("/delete")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted a user.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SuccessResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User is not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
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
                    responseCode = "404",
                    description = "Not Found - User with the given username was not found",
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
            )
    }
    )
    public ResponseEntity<?> delete(@RequestParam(value = "username", required = false) String username, HttpServletRequest request) {

        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);


        if (username == null || username.equals(user.get().getUsername())) {
            boolean deleteReq = UserManager.delete(user.get());
            if (deleteReq)
                return ResponseEntity.ok(new SuccessResponseDto("User deleted successfully"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
        }

        Optional<User> userFromParam = UserManager.get(username);
        if (userFromParam.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponseDto.Error.USER_NOT_FOUND);
        }


        boolean isAdmin = user.get().isAdministrator();


        if (isAdmin) {
            boolean deleteReq = UserManager.delete(username);
            if (deleteReq)
                return ResponseEntity.ok(new SuccessResponseDto("User deleted successfully"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
    }


}
