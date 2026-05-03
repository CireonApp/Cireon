package com.cireonapp.server.domain.session;

import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;

@Entity(value = "sessions")
public class Session {
    @Id
    private String token;
    private String username;
    private String creationTime;
    private String device;

    public Session(String token, String username, String creationTime, String device) {
        this.token = token;
        this.username = username;
        this.creationTime = creationTime;
        this.device = device;
    }

    public Session() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
