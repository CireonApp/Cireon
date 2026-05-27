package com.cireonapp.server.util;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
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

    public static Optional<User> getUserFromSessionCookie(HttpServletRequest request) {
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);

        if (cookie.isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isEmpty()) {
            return Optional.empty();
        }

        return UserManager.get(session.get().getUsername());

    }
}
