package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.CheckSessionResponseDto;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.dto.SuccessResponseDto;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

import static com.cireonapp.server.controller.backend.AuthenticationController.AUTH_COOKIE_NAME;


@Tag(name = "Session API", description = "Session related endpoints. Requires authentication.")
@RestController
@RequestMapping("/api/auth/session")
public class SessionController {

    @Operation(
            summary = "Check current session.",
            description = "Check current user session. User needs to be authenticated to use this endpoint"
    )
    @GetMapping("/check")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the current session.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CheckSessionResponseDto.class
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
            )
    })
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        Optional<Cookie> authCookie = CookieHelper.getAuthCookie(request);

        if (authCookie.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        Optional<Session> session = SessionManager.get(authCookie.get().getValue());

        if (session.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        String username = session.get().getUsername();
        if (username == null || username.isBlank()) {
            SessionManager.delete(authCookie.get().getValue());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        Optional<User> user = UserManager.get(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CheckSessionResponseDto(user.get().getUsername(), user.get().getDisplayName(), user.get().isAdministrator(), user.get().getSettings()));
        //TODO: maybe for future just pass the user and handle the Dto in the class itself...
    }

    @Operation(
            summary = "Revoke any session.",
            description = "Revoke any session. User needs to be authenticated to use this endpoint. You can only revoke your own sessions or if you have the right permissions, you can revoke any session.  If you want to revoke own session without session id, you may use the logout method"

    )
    @DeleteMapping("/revoke")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully revoked the session.",
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
                    responseCode = "400",
                    description = "Bad Request - Missing or invalid parameters",
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
                    responseCode = "500",
                    description = "Internal Server Error - An error occurred while processing your request.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
    })
    public static ResponseEntity<?> revokeSession(HttpServletRequest request, @RequestParam(value = "token") String tokenToRevoke) {
        Optional<Cookie> authCookie = CookieHelper.getAuthCookie(request);

        if (authCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        Optional<Session> currentSession = SessionManager.get(authCookie.get().getValue());

        if (currentSession.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        if (tokenToRevoke == null || tokenToRevoke.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.MISSING_PARAMETERS);
        }

        Optional<Session> sessionToRevoke = SessionManager.get(tokenToRevoke);

        if (sessionToRevoke.isEmpty()) {
            // Do not reveal that the session does not exist, can be used to check if a token is valid or not.
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
        }

        boolean ownSession = Objects.equals(sessionToRevoke.get().getUsername(), currentSession.get().getUsername());

        Optional<User> currentUser = UserManager.get(currentSession.get().getUsername());

        if (currentUser.isEmpty())
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);


        boolean isAdmin = currentUser.get().isAdministrator();


        if (!isAdmin && !ownSession) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
        }

        SessionManager.delete(tokenToRevoke);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuccessResponseDto("Session revoked successfully!"));
    }

    @Operation(
            summary = "Revoke all sessions of a user.",
            description = "Revoke all sessions of a user. User needs to be authenticated to use this endpoint. You may choose if you want to revoke the current session or not. If you choose to revoke the current session, the user will be logged out."
    )
    @DeleteMapping("/revokeAll")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully revoked all sessions.",
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
                    responseCode = "400",
                    description = "Bad Request - Missing or invalid parameters",
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
    })
    public ResponseEntity<?> revokeAllSessions(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "current") Boolean revokeCurrent) {
        boolean revokeCurrentSession = revokeCurrent;
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);
        if (cookie.isEmpty()) {
            Cookie newCookie = new Cookie(AUTH_COOKIE_NAME, "");
            newCookie.setMaxAge(0); // 1 Year
            newCookie.setPath("/");
            response.addCookie(newCookie);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }
        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isPresent()) {
            SessionManager.deleteAllForUser(session.get().getUsername(),cookie.get().getValue(), revokeCurrentSession);
            if (revokeCurrentSession) {
                cookie.get().setValue("");
                cookie.get().setMaxAge(0);
                cookie.get().setPath("/");
                response.addCookie(cookie.get());
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SuccessResponseDto("All sessions revoked successfully!"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseDto.Error.NOT_LOGGED_IN);
    }


}
