package com.cireonapp.server.service.interceptor;

import com.cireonapp.server.domain.config.ConfigManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class FirstTimeSetupInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!ConfigManager.get().isFirstTimeSetupComplete()) {
            response.sendRedirect("/setup");
            return false;
        }
        return true;
    }
}
