package com.cireonapp.server.domain.config;

import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;

import java.util.Date;


@Entity(value = "config")
public class Config {
    private int port = 50262;
    private int maxUsers = 8;

    public Config() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

//    public Config(Optional<Integer> port, Optional<Integer> maxUsers) {
//        this.port = port.orElse(50262);
//        this.maxUsers = maxUsers.orElse(8); // Default value
//    }

}
