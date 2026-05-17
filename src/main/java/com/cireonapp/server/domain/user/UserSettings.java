package com.cireonapp.server.domain.user;

import com.cireonapp.server.util.WebThemes;

public class UserSettings {
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
