package com.cireonapp.server.dto;

import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.util.TimeHelper;

import java.util.Set;

public class LoginResponseDto extends ResponseDto {
    public String displayName;
    public Set<UserPermissions> permissions;

    public LoginResponseDto(String displayName, Set<UserPermissions> permissions) {
        this.displayName = displayName;
        this.permissions = permissions;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}