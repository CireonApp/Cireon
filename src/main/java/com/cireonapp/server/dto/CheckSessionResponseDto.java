package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.util.TimeHelper;

import java.util.Set;

public class CheckSessionResponseDto extends ResponseDto {
    public String displayName;
    public String username;
    public Set<UserPermissions> permissions;

    public CheckSessionResponseDto(String username, String displayName, Set<UserPermissions> permissions) {
        this.username = username;
        this.displayName = displayName;
        this.permissions = permissions;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}