package com.cireonapp.server;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import com.cireonapp.server.initializer.SourceSubscription;
import com.cireonapp.server.util.DataDirHelper;
import jakarta.annotation.PreDestroy;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));


        DataDirHelper.initializeDataDir();
        Databases.bootstrap();

        SpringApplication app = new SpringApplication(ServerApplication.class);

        Config config = ConfigManager.get();

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", config.getPort());
        props.put("application.title", model.getName());
        props.put("application.version", model.getVersion());

        app.setDefaultProperties(props);

        app.addInitializers(new FileWatcher(),new SourceSubscription());

        application = app.run(args);
    }



    public static void restart() {
        application.restart();
    }

}
