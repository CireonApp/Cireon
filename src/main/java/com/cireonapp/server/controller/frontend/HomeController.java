package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {
    @GetMapping("/")
    String home(Model model, HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);
        if (cookie.isEmpty()) {
            return "redirect:/login";
        }

        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isEmpty()) {
            return "redirect:/login";
        }

        Optional<User> user = UserManager.get(session.get().getUsername());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("displayName", user.get().getDisplayName());
        return "home/index";
    }
}
