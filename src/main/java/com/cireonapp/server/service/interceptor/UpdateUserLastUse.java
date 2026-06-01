package com.cireonapp.server.service.interceptor;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

public class UpdateUserLastUse implements HandlerInterceptor {
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        user.ifPresent(UserManager::updateLastUse);
    }
}
