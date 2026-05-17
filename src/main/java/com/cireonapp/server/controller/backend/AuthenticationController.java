package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.Authentication;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.*;
import com.cireonapp.server.util.CookieHelper;
import com.cireonapp.server.util.EncryptionHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    public static final String AUTH_COOKIE_NAME = "SessionToken";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authHeader, HttpServletResponse response, HttpServletRequest request) {
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);

        if (cookie.isPresent() && SessionManager.isValid(cookie.get().getValue()))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Already logged in! please log out first."));

        if (!authHeader.startsWith("Basic ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Invalid Authorization format."));
        }
        String encodedCreds = authHeader.split(" ")[1];

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encodedCreds));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Invalid Base64 encoding in Authorization header!"));
        }
        int separator = decoded.indexOf(":");
        if (separator == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Invalid Authorization format. Expected 'username:password' after Base64 decoding."));
        }

        String username = decoded.substring(0, separator);
        String password = decoded.substring(separator + 1);

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Username or password cannot be empty!"));
        }

        Optional<User> user = UserManager.get(username);

        if (user.isEmpty()) {
            EncryptionHelper.encryptPassword_argon2(password);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.INCORRECT_USERNAME_PASSWORD);
        }

        boolean successful = Authentication.authenticateExistingUser(user.get(), password);

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
        newCookie.setMaxAge(31536000); // 1 Year
        newCookie.setSecure(false); // Does not matter since it's a local app to run on a home server...
        newCookie.setHttpOnly(true);
        newCookie.setPath("/");
        newCookie.setValue(session.get().getToken());
        response.addCookie(newCookie);


        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginResponseDto(user.get().getDisplayName(), user.get().getPermissions()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> authCookie = CookieHelper.getAuthCookie(request);

        if(authCookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SuccessResponseDto("Logged out successfully!"));
    }


    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleMissingRequestHeaderException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto("Missing Authorization header!"));
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleArrayIndexOutOfBoundsException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto("Username or password is empty!"));
    }
}
