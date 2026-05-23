package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.Source;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;

import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
import static java.nio.file.StandardWatchEventKinds.*;


public class FileWatcher implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static WatchService watcher;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ServerApplication.LOGGER.info("Initializing file watching service...");
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread watcherThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watcher.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        System.out.println(event.kind() + " " + event.context());
                        // Handle file change events...
                    }

                    key.reset();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        watcherThread.setDaemon(true);
        watcherThread.start();

    }

    private static final Map<String, WatchKey> registeredSources = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean registerSource(Source source) {
        if (registeredSources.containsKey(source.getId())) {
            ServerApplication.LOGGER.warn("Attempted to register path that is already registered: " + source.getDirPath());
            return false;
        }
        try {
            WatchKey key = Path.of(source.getDirPath().toString()).register(watcher, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE}, FILE_TREE);
            registeredSources.put(source.getId(), key);
            ServerApplication.LOGGER.info("Registered path for watching: " + source.getDirPath());
            handleRegisteredSources(source);
            return true;
        } catch (IOException e) {
            ServerApplication.LOGGER.warn("Failed to register path for watching: " + source.getDirPath(), e);
            return false;
        }
    }

    public static boolean UnregisterSource(Source source) {
        if (registeredSources.containsKey(source.getId())) {
            registeredSources.get(source.getId()).cancel();
            registeredSources.remove(source.getId());
            return true;
        } else {
            ServerApplication.LOGGER.warn( "Attempted to unregister path that was not registered: " + source.getDirPath());
            return false;
        }
    }

    public static Set<String> GetRegisteredPaths() {
        return registeredSources.keySet();
    }


    private static void handleRegisteredSources(Source source) {
        switch (source.getType()) {
            case MOVIE:
                MovieManager.handleMovieSourceUpdate(source);
                break;
            case TV_SHOW:
                break;
        }
    }

}
