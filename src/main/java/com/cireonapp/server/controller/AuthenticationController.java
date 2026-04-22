package com.cireonapp.server.controller;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.dto.LoginResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestHeader("Authorization") String authHeader, HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("AUTH")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ErrorResponseDto("Already logged in! please log out first."));
                }
            }


        String encodedCreds = authHeader.split(" ")[1];
        String credentials = new String(Base64.getDecoder().decode(encodedCreds));
        String username = credentials.split(":")[0];
        String password = credentials.split(":")[1];

        Optional<User> user = UserManager.getUser(username);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Invalid username or password"));
        }

        if (!user.get().getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponseDto("Invalid username or password"));
        }

        Cookie cookie = new Cookie("AUTH", RequestContextHolder.currentRequestAttributes().getSessionId());
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setValue(encodedCreds);
        response.addCookie(cookie);


        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginResponseDto(user.get().getDisplayName(), user.get().getPermissions()));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("AUTH")) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ErrorResponseDto("Logged out successfully!"));
                }
            }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto("Not logged in!"));
    }

}
