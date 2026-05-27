package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {
    @GetMapping("/login")
    String login(Model model, HttpServletRequest request, @RequestParam(name = "username", required = false) String username) {

//        if(UserManager.getCount() == 0){
//            return "redirect:/setup";
//        TODO setup does not exists yet! For future create one and redirect.
//        }


        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty()) {
            return "redirect:/";
        }

        if (username != null) {
            model.addAttribute("usernameInput", username);
        }

        model.addAttribute("theme", UserManager.getThemeLabel(null).label);

        return "login/index";
    }

}
