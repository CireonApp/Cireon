package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class SignupController {
    @GetMapping("/signup")
    String signup(Model model, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isPresent()) {
            return "redirect:/";
        }


        model.addAttribute("theme", UserManager.getThemeLabel(null).label);

        return "signup/index";
    }

}
