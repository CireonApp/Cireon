package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
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
        ServerApplication.LOGGER.log(java.util.logging.Level.INFO, "Initializing file watching service...");
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

    public static boolean RegisterPath(String path) {
        try {
            WatchKey key = Path.of(path).register(watcher, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE}, FILE_TREE);
            registeredPaths.put(path, key);
            ServerApplication.LOGGER.log(java.util.logging.Level.SEVERE, "Registered path for watching: " + path);

            return true;
        } catch (IOException e) {
            ServerApplication.LOGGER.log(java.util.logging.Level.SEVERE, "Failed to register path for watching: " + path, e);
            return false;
        }
    }

    public static boolean UnregisterPath(String path) {
        if (registeredPaths.containsKey(path)) {
            registeredPaths.get(path).cancel();
            registeredPaths.remove(path);
            return true;
        } else {
            ServerApplication.LOGGER.log(java.util.logging.Level.WARNING, "Attempted to unregister path that was not registered: " + path);
            return false;
        }
    }

    public static Set<String> GetRegisteredPaths() {
        return registeredPaths.keySet();
    }

}
