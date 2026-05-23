package com.cireonapp.server;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.initializer.AppPath;
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

    public static void main(String[] args) throws Throwable {
        try {
            silenceLogger("nitrite");
            silenceLogger("org.dizitart");

            DataDirHelper.initializeDataDir();
            Databases.bootstrap();
            Config config = ConfigManager.get();
            Databases.shutdown();

            unsilenceLogger("nitrite");
            unsilenceLogger("org.dizitart");

            SpringApplication app = new SpringApplication(ServerApplication.class);
            Databases.logInitialized = true;
            app.addInitializers(new Databases());

            // Only set dynamic runtime properties here, NOT the banner info
            Map<String, Object> props = new HashMap<>();
            props.put("server.port", config.getPort());
            app.setDefaultProperties(props);

            app.addInitializers(new FileWatcher(), new SourceSubscription());
            application = app.run(args);
            LOGGER.info("Data folder location: {}", AppPath.APP_DIR);
            LOGGER.info("Server started on port {}", config.getPort());
        } catch (Throwable e) {
            // DevTools throws SilentExitException intentionally on restart — let it through
            if (!e.getClass().getName().contains("SilentExitException")) {
                if (e.getClass().getName().contains("PortInUseException"))
                    LOGGER.warn("Port is already in use!");
                System.out.println("\nPress Enter to close...");
                new Scanner(System.in).nextLine();
                System.exit(1);
            }
        }
    }

    public static void restart() {
        application.restart();
    }

    private static void silenceLogger(String name) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name)).setLevel(Level.OFF);
    }

    private static void unsilenceLogger(String name) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name)).setLevel(Level.TRACE);
    }
}
