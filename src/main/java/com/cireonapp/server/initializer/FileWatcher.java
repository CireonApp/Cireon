package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.Source;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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

    private static final Map<String, WatchKey> registeredPaths = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean RegisterPath(Source source) {
        if (registeredPaths.containsKey(source.getDirPath().toString())) {
            ServerApplication.LOGGER.warn("Attempted to register path that is already registered: " + source.getDirPath());
            return false;
        }
        try {
            WatchKey key = Path.of(source.getDirPath().toString()).register(watcher, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE}, FILE_TREE);
            registeredPaths.put(source.getDirPath().toString(), key);
            ServerApplication.LOGGER.info("Registered path for watching: " + source.getDirPath());
            handleRegisteredPaths(source);
            return true;
        } catch (IOException e) {
            ServerApplication.LOGGER.warn("Failed to register path for watching: " + source.getDirPath(), e);
            return false;
        }
    }

    public static boolean UnregisterPath(String path) {
        if (registeredPaths.containsKey(path)) {
            registeredPaths.get(path).cancel();
            registeredPaths.remove(path);
            return true;
        } else {
            ServerApplication.LOGGER.warn( "Attempted to unregister path that was not registered: " + path);
            return false;
        }
    }

    public static Set<String> GetRegisteredPaths() {
        return registeredPaths.keySet();
    }


    private static void handleRegisteredPaths(Source source) {
        switch (source.getType()) {
            case MOVIE:
                MovieManager.handleMovieSourceUpdate(source);
                break;

            case TV_SHOW:
                break;
        }
    }

}
