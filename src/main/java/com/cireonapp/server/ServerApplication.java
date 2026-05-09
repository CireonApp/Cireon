package com.cireonapp.server;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import com.cireonapp.server.initializer.SourceSubscription;
import com.cireonapp.server.util.DataDirHelper;
import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


@SpringBootApplication
public class ServerApplication {
    private static ConfigurableApplicationContext application;

    public static final Logger LOGGER =
            LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        try {
            // Silence Nitrite before Spring starts — logback-spring.xml profiles aren't active yet
            silenceLogger("nitrite");
            silenceLogger("org.dizitart");

            DataDirHelper.initializeDataDir();
            Databases.bootstrap();
            Config config = ConfigManager.get();
            Databases.shutdown();

            SpringApplication app = new SpringApplication(ServerApplication.class);
            app.addInitializers(new Databases());

            // Only set dynamic runtime properties here, NOT the banner info
            Map<String, Object> props = new HashMap<>();
            props.put("server.port", config.getPort());
            app.setDefaultProperties(props);

            app.addInitializers(new FileWatcher(), new SourceSubscription());
            application = app.run(args);
        } catch (Exception e) {
            LOGGER.error("Fatal error during startup: {}", e.getMessage(), e);
            System.out.println("\nPress Enter to close...");
            new Scanner(System.in).nextLine();
            System.exit(1);
        }
    }

    public static void restart() {
        application.restart();
    }

    private static void silenceLogger(String name) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name)).setLevel(Level.OFF);
    }

}
