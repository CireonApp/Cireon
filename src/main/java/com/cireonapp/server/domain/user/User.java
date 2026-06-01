package com.cireonapp.server.domain.user;

import com.cireonapp.server.util.TimeHelper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;


@Entity(value = "users", indices = {
        @Index(fields = "username"),
        @Index(fields = "displayName", type = IndexType.NON_UNIQUE),
        @Index(fields = "password", type = IndexType.NON_UNIQUE),
        @Index(fields = "administrator", type = IndexType.NON_UNIQUE)
})
public class User {
    @Id
    private String username;
    private String password;
    private String displayName;
    private boolean administrator;
    private UserSettings settings;
    private String creationDate;
    private String lastUseDate;

    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        if (displayName == null || displayName.isBlank()) {
            this.displayName = username;
        } else {
            this.displayName = displayName;
        }
        this.administrator = false;
        this.settings = UserSettings.getDefault();
        this.creationDate = TimeHelper.getCurrentTimeISO();
        this.lastUseDate = TimeHelper.getCurrentTimeISO();
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getLastUseDate() {
        return lastUseDate;
    }

    public void updateLastUseDate() {
        this.lastUseDate = TimeHelper.getCurrentTimeISO();
    }

    public void setLastUseDate(String lastUseDate) {
        this.lastUseDate = lastUseDate;
    }
}
