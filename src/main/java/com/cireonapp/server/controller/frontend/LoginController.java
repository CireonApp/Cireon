package com.cireonapp.server.controller.frontend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    String login(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("something","this is a test");

        return "login/index";
    }

}
