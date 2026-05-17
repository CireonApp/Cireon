package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.domain.user.UserSettings;
import com.cireonapp.server.util.TimeHelper;

import java.util.Set;

public class CheckSessionResponseDto extends ResponseDto {
    public String displayName;
    public String username;
    public Set<UserPermissions> permissions;
    public UserSettings settings;

    public CheckSessionResponseDto(String username, String displayName, Set<UserPermissions> permissions, UserSettings settings) {
        this.username = username;
        this.displayName = displayName;
        this.permissions = permissions;
        this.settings = settings;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}