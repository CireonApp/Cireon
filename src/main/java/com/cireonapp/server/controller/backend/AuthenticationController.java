package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.Authentication;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.*;
import com.cireonapp.server.util.CookieHelper;
import com.cireonapp.server.util.EncryptionHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.cireonapp.server.domain.session.SessionManager.SESSION_EXPIRATION_TIME_SECONDS;

@Tag(name = "Authentication API", description = "Authentication related endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    public static final String AUTH_COOKIE_NAME = "SessionToken";

    @Operation(
            summary = "Login with username and password.",
            description = "Login with username and password. If successful, returns a session cookie. If already logged in, returns an error. If username or password is incorrect, returns an error."
    )
    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - already logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Incorrect username or password",
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
                    description = "Successfully logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = LoginResponseDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto userCred, HttpServletResponse response, HttpServletRequest request) {
        Optional<User> userFromSession = CookieHelper.getUserFromSessionCookie(request);

        if (userFromSession.isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Already logged in! please log out first."));


        Optional<User> user = UserManager.get(userCred.username);

        if (user.isEmpty()) {
            EncryptionHelper.encryptPassword_argon2(userCred.password);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INCORRECT_USERNAME_PASSWORD);
        }

        boolean successful = Authentication.authenticateExistingUser(user.get(), userCred.password);

        if (!successful) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INCORRECT_USERNAME_PASSWORD);
        }

        Optional<Session> session = SessionManager.create(user.get().getUsername(), "");

        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
        }

        Cookie newCookie = new Cookie(AUTH_COOKIE_NAME, session.get().getToken());
        newCookie.setMaxAge((int) SESSION_EXPIRATION_TIME_SECONDS);
        newCookie.setSecure(false); // Does not matter since it's a local app to run on a home server...
        newCookie.setHttpOnly(true);
        newCookie.setPath("/");
        newCookie.setValue(session.get().getToken());
        response.addCookie(newCookie);


        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginResponseDto(user.get()));
    }

    @Operation(
            summary = "Logout current user..",
            description = "If user is logged in, will log out the user by deleting the session and clearing the cookie. If no valid session is found, returns an error."
    )
    @PostMapping("/logout")
    @ApiResponses(value = {
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
                    description = "Successfully logged out",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SuccessResponseDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> authCookie = CookieHelper.getAuthCookie(request);

        if (authCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }
        Cookie cookie = authCookie.get();

        boolean validSession = SessionManager.isValid(cookie.getValue());
        SessionManager.delete(cookie.getValue());
        cookie.setValue("");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        if (!validSession) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuccessResponseDto("Logged out successfully!"));
    }
}
