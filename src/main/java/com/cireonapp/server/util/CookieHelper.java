package com.cireonapp.server.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

import static com.cireonapp.server.controller.backend.AuthenticationController.AUTH_COOKIE_NAME;

public class CookieHelper {
    public static Optional<Cookie> getAuthCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(c -> AUTH_COOKIE_NAME.equals(c.getName()))
                .findFirst();
    }
}
