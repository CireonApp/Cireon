package com.cireonapp.server.initializer;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.user.User;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.jackson.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

import static com.cireonapp.server.initializer.AppPath.APP_DIR;

@Component
public class Databases {
    private static Nitrite database;
    public static ObjectRepository<User> userRepository;
    public static ObjectRepository<Config> configRepository;
    public static ObjectRepository<Session> sessionRepository;
    public static ObjectRepository<Source> sourceRepository;
    public static ObjectRepository<Movie> movieRepository;
    private static boolean initialized = false;


    public static synchronized void bootstrap() throws IOException {
        if (initialized) return;
        initialized = true;
        if (!Files.exists(APP_DIR.resolve("data")))
            Files.createDirectory(APP_DIR.resolve("data"));

        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(APP_DIR.resolve("data/database").toString())
                .build();



        database = Nitrite.builder()
//                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule(new JavaTimeModule()))
//                .registerEntityConverter(new UserConverter())
//                .registerEntityConverter(new ConfigConverter())
//                .registerEntityConverter(new SessionConverter())
//                .registerEntityConverter(new ArtworkConverter())
                .openOrCreate();


        userRepository = database.getRepository(User.class, "users");
        configRepository = database.getRepository(Config.class, "config");
        sessionRepository = database.getRepository(Session.class, "sessions");
        sourceRepository = database.getRepository(Source.class, "sources");
        movieRepository = database.getRepository(Movie.class, "movies");
        firstTimeSetup();
    }

    private static void firstTimeSetup() {
        if (!configRepository.find().toSet().isEmpty()) return;
        Config config = new Config();
        //read line to ask for settings.

        configRepository.insert(config);
    }

    @PreDestroy
    public void shutdown() {
        if (database != null && !database.isClosed()) {
            database.close(); // Crucial to release the file lock for DevTools restart
        }
    }
}
