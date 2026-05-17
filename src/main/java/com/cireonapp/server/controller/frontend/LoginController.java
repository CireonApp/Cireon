package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.session.SessionManager;
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
public class LoginController {
    @GetMapping("/login")
    String login(Model model, HttpServletRequest request, HttpServletResponse response) {

//        if(UserManager.getCount() == 0){
//            return "redirect:/setup";
//        setup does not exists yet! For future create one and redirect.
//        }

        model.addAttribute("theme", UserManager.getThemeLabel(null).label);

        Optional<Cookie> optCookie = CookieHelper.getAuthCookie(request);

        if(optCookie.isPresent()){
            Cookie cookie = optCookie.get();
            if(SessionManager.isValid(cookie.getValue())){
                return "redirect:/";
            }
        }

        return "login/index";
    }

}
