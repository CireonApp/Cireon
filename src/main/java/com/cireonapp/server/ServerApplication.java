package com.cireonapp.server;

import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.logging.Logger;

@SpringBootApplication
public class ServerApplication {

    public static Logger LOGGER = Logger.getLogger("Cireon Backend Server");

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApplication.class);

        app.addInitializers(new Databases());
        app.addInitializers(new FileWatcher());

        app.setDefaultProperties(Collections
                .singletonMap("server.port", "50262"));

        app.run(args);
    }

}
