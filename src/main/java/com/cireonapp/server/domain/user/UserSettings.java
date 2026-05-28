package com.cireonapp.server.domain.user;

import com.cireonapp.server.util.WebThemes;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "User Settings",
        description = "User settings. Contains the web theme of the user."
)
public class UserSettings {
    @Schema(description = "Web theme of the user interface.")
    private WebThemes webTheme;

    public static UserSettings getDefault() {
        UserSettings settings = new UserSettings();
        settings.setWebTheme(WebThemes.DARK);
        return settings;
    }

    public UserSettings() {
    }

    public UserSettings(WebThemes webTheme) {
        this.webTheme = webTheme;
    }


    public WebThemes getWebTheme() {
        return webTheme;
    }

    public void setWebTheme(WebThemes webTheme) {
        this.webTheme = webTheme;
    }
}
