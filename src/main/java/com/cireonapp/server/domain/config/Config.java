package com.cireonapp.server.domain.config;

import io.swagger.v3.oas.annotations.media.Schema;
import org.dizitart.no2.repository.annotations.Entity;



@Entity(value = "config")
@Schema(hidden = true)
public class Config {
    @Schema(defaultValue = "50262", description = "The port the server is listening to")
    private int port = 50262;
    @Schema(defaultValue = "8", description = "Max amount of users that can be created. Admins can create without limitations")
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
