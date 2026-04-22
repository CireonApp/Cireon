package com.cireonapp.server.domain.user;

import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.Set;


@Entity(value = "users", indices = {
        @Index(fields = "username"),
        @Index(fields = "displayName", type = IndexType.NON_UNIQUE),
        @Index(fields = "password", type = IndexType.NON_UNIQUE),
        @Index(fields = "permissions", type = IndexType.NON_UNIQUE)
})
public class User {
    @Id
    private String username;
    private String password;
    private String displayName;
    private Set<UserPermissions> permissions;

    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        if(displayName == null || displayName.isBlank()) {
            this.displayName = username;
        } else {
            this.displayName = displayName;
        }
        this.permissions = Set.of(UserPermissions.USER_READ, UserPermissions.CONTENT_READ);
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

    public Set<UserPermissions> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermissions> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", displayName='" + displayName + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
