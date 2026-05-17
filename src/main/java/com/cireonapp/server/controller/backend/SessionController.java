package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.dto.*;
import com.cireonapp.server.util.CookieHelper;
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

@RestController
@RequestMapping("/api/auth/session")
public class SessionController {
    @GetMapping("/check")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        for (Cookie cookie : cookies) {
            if (!cookie.getName().equals(AUTH_COOKIE_NAME)) continue;
            Optional<Session> session = SessionManager.get(cookie.getValue());
            if (session.isPresent()) {
                String username = session.get().getUsername();
                if (username == null || username.isBlank()) {
                    SessionManager.delete(session.get().getToken());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(CommonResponseDto.Error.NOT_LOGGED_IN);
                }

                Optional<User> user = UserManager.get(username);
                if (user.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
                }
                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new CheckSessionResponseDto(user.get().getUsername(), user.get().getDisplayName(), user.get().getPermissions(),user.get().getSettings()));
                        //TODO: maybe for future just pass the user and handle the Dto in the class itself...
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(CommonResponseDto.Error.NOT_LOGGED_IN);
    }

    @PostMapping("/revoke")
    public static ResponseEntity<?> revokeSession(HttpServletRequest request, @RequestParam(value = "token", required = false) String tokenToRevoke) {
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

        if(tokenToRevoke == null || tokenToRevoke.isBlank()) {
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


        boolean hasPermission = currentUser.get().getPermissions().contains(UserPermissions.ADMINISTRATOR) ||
                currentUser.get().getPermissions().contains(UserPermissions.USER_MANAGE);


        if (!hasPermission && !ownSession) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
        }

        SessionManager.delete(sessionToRevoke.get().getToken());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuccessResponseDto("Session revoked successfully!"));
    }

    @PostMapping("/revokeAll")
    public ResponseEntity<?> revokeAllSessions(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "current", defaultValue = "false") String revokeCurrent) {
        boolean revokeCurrentSession = Objects.equals(revokeCurrent, "true");
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);
        if (cookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }
        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isPresent()) {
            SessionManager.deleteAllForUser(session.get().getUsername(), session.get().getToken(), revokeCurrentSession);
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
