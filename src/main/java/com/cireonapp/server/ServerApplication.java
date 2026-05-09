package com.cireonapp.server;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import com.cireonapp.server.initializer.SourceSubscription;
import com.cireonapp.server.util.DataDirHelper;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@SpringBootApplication
public class ServerApplication {
    private static ConfigurableApplicationContext application;

    public static Logger LOGGER = Logger.getLogger("Cireon Backend Server");

    public static void main(String[] args) throws Exception {
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
    }

    public static void restart() {
        application.restart();
    }

}
