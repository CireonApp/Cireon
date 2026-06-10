package com.cireonapp.server.domain.user;

import com.cireonapp.server.util.EncryptionHelper;
import com.cireonapp.server.util.TimeHelper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.Objects;


@Entity(value = "users", indices = {
        @Index(fields = "username"),
        @Index(fields = "displayName", type = IndexType.NON_UNIQUE),
        @Index(fields = "password", type = IndexType.NON_UNIQUE),
        @Index(fields = "administrator", type = IndexType.NON_UNIQUE),
        @Index(fields = "allowAdultContent", type = IndexType.NON_UNIQUE),
        @Index(fields = "creationDate", type = IndexType.NON_UNIQUE),
        @Index(fields = "lastUseDate", type = IndexType.NON_UNIQUE)
})
public class User {
    @Id
    private String username;
    private String password;
    private String displayName;
    private Boolean administrator;
    private UserSettings settings;
    private String creationDate;
    private String lastUseDate;
    private Boolean allowAdultContent;

    public User(String username, String password, String displayName, Boolean allowAdultContent) {
        this.username = username;
        this.password = password;
        if (displayName == null || displayName.isBlank()) {
            this.displayName = username;
        } else {
            this.displayName = displayName;
        }

        this.allowAdultContent = allowAdultContent;
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

    /**
     * Merge user data with new data. passwords will be hashed in this method.
     * if a field is null it will not be merged and the old value will be kept.
     * Will merge user settings. To only change settings just pass newData with only the settings changed.
     *
     * @param newData new user data.
     */
    public void merge(User newData) {
        if (newData == null) return;
        if (newData.displayName != null)
            this.displayName = newData.displayName;
        if (newData.password != null)
            this.password = EncryptionHelper.encryptPassword_argon2(this.password);
        if (newData.administrator != null)
            this.administrator = newData.administrator;
        if (newData.allowAdultContent != null)
            this.allowAdultContent = newData.allowAdultContent;
        if (newData.settings != null)
            newData.settings.merge(this.settings);

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

    public Boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(Boolean administrator) {
        this.administrator = administrator;
        if (administrator == true) {
            // Restricting content from admins is awkward.
            this.allowAdultContent = false;
        }
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

    public Boolean getAllowAdultContent() {
        return Objects.requireNonNullElse(allowAdultContent, true);
    }

    public void setAllowAdultContent(Boolean allowAdultContent) {
        this.allowAdultContent = allowAdultContent;
    }
}
