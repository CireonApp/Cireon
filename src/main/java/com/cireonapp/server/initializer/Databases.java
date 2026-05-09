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
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static com.cireonapp.server.initializer.AppPath.APP_DIR;

@Configuration
public class Databases implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static Nitrite database;
    public static ObjectRepository<Config> configRepository;
    public static ObjectRepository<User> userRepository;
    public static ObjectRepository<Session> sessionRepository;
    public static ObjectRepository<Source> sourceRepository;
    public static ObjectRepository<Movie> movieRepository;

    public static synchronized void bootstrap() throws IOException {
        if (database != null && !database.isClosed()) {
            return;
        }

        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(APP_DIR.resolve("data/database").toString())
                .build();

        database = Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule(new JavaTimeModule()))
                .openOrCreate();

        userRepository = database.getRepository(User.class, "users");
        configRepository = database.getRepository(Config.class, "config");
        sessionRepository = database.getRepository(Session.class, "sessions");
        sourceRepository = database.getRepository(Source.class, "sources");
        movieRepository = database.getRepository(Movie.class, "movies");

        if (configRepository.find().firstOrNull() == null) {
            configRepository.insert(new Config());
        }
    }

    @Bean
    public Nitrite nitrite() {
        return database;
    }

    @PreDestroy
    public static void shutdown() {
        if (database != null && !database.isClosed()) {
            database.close();
        }
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        try {
            bootstrap();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
}