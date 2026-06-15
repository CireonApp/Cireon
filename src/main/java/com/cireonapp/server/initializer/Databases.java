package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.watchProgress.UserVideoWatchProgress;
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

    private static final MVStoreModule storeModule = MVStoreModule.withConfig()
            .filePath(APP_DIR.resolve("data/database").toString())
            .build();

    private static Nitrite database;
    private static ObjectRepository<Config> configRepository;
    private static ObjectRepository<User> userRepository;
    private static ObjectRepository<Session> sessionRepository;
    private static ObjectRepository<Source> sourceRepository;
    private static ObjectRepository<Movie> movieRepository;
    public static ObjectRepository<UserVideoWatchProgress> watchProgressRepository;

    /**
     * initializing the database will set this to false to prevent double logging.
     */
    public static boolean logInitialized = false;


    public static synchronized void bootstrap() throws IOException {
        if (logInitialized) {
            ServerApplication.LOGGER.info("Initializing database...");
            logInitialized = false;
        }

        if (database != null && !database.isClosed()) {
            return;
        }

        openDatabase();
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


    private static void openDatabase() {
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

    private static boolean isRepositoryClosed(ObjectRepository<?> repo) {
        return repo == null || !repo.isOpen();
    }


    public static ObjectRepository<Config> getConfigRepository() {
        if (isRepositoryClosed(configRepository) && database.isClosed()) {
            openDatabase();
        }
        return configRepository;
    }

    public static ObjectRepository<User> getUserRepository() {
        if (isRepositoryClosed(userRepository) && database.isClosed()) {
            openDatabase();
        }
        return userRepository;
    }

    public static ObjectRepository<Session> getSessionRepository() {
        if (isRepositoryClosed(sessionRepository) && database.isClosed()) {
            openDatabase();
        }
        return sessionRepository;
    }

    public static ObjectRepository<Source> getSourceRepository() {
        if (isRepositoryClosed(sourceRepository) && database.isClosed()) {
            openDatabase();
        }
        return sourceRepository;
    }

    public static ObjectRepository<Movie> getMovieRepository() {
        if (isRepositoryClosed(movieRepository) && database.isClosed()) {
            openDatabase();
        }
        return movieRepository;
    }

    public static ObjectRepository<UserVideoWatchProgress> getWatchProgressRepository() {
        if (isRepositoryClosed(watchProgressRepository) && database.isClosed()) {
            openDatabase();
        }
        return watchProgressRepository;
    }
}