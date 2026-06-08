package com.cireonapp.server.controller.frontend;

import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.media.source.SourceManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class AdminPanelController {
    @GetMapping("/admin")
    String adminPanel() {
        return "redirect:/admin/config";
    }

    @GetMapping("/admin/{settings:users|config|content}")
    String adminPanelWithContent(@PathVariable String settings, Model model, HttpServletRequest request) {

        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty() || !user.get().isAdministrator())
            return "redirect:/";

        model.addAttribute("settings", settings);
        model.addAttribute("user", user.get());

        // no need to fetch unused content.
        switch (settings) {
            case "users":
                model.addAttribute("userList", UserManager.getAll().toList());
                break;
            case "config":
                model.addAttribute("currentSettings", ConfigManager.get());
                break;

            case "content":
                model.addAttribute("sourceList", SourceManager.getAll().toList());
        }

        return "admin/index";
    }
}
