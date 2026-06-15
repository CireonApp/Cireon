package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.Source;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    // Helper to keep track of a source's lifecycle
    private record SourceWatcher(WatchService service, Thread thread) {
    }

    private static final Map<String, SourceWatcher> activeWatchers = new ConcurrentHashMap<>();

    public static void registerSource(Source source) {
        if (activeWatchers.containsKey(source.getId())) {
            return;
        }

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = Path.of(source.getDirPath().toString());

            // Register with the specific service created for this source
            path.register(watcher, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE}, FILE_TREE);

            Thread watcherThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watcher.take();
                        List<WatchEvent<?>> events = key.pollEvents();

                        Map<Path, WatchEvent<?>> lastEventPerFile = new LinkedHashMap<>();
                        for (WatchEvent<?> event : events) {
                            if (event.context() instanceof Path filename) {
                                lastEventPerFile.put(filename, event);
                            }
                        }

                        // 2. Now process the final state of each file
                        for (Map.Entry<Path, WatchEvent<?>> entry : lastEventPerFile.entrySet()) {
                            Path eventPath = source.getDirPath().resolve(entry.getKey());

                            WatchEvent.Kind<?> kind = entry.getValue().kind();

                            if (kind == ENTRY_DELETE) {
                                handleDeleteEvent(source, eventPath);
                            }
                            if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {
                                handleModifyEvent(source, eventPath);
                            }
                        }

                        if (!key.reset()) break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            watcherThread.setDaemon(true);
            watcherThread.start();

            activeWatchers.put(source.getId(), new SourceWatcher(watcher, watcherThread));
        } catch (IOException e) {
        }
    }

    public static void handleDeleteEvent(Source source, Path eventPath) {
        boolean lookedLikeDirectory = !eventPath.toString().contains(".");

        if (lookedLikeDirectory) {
            switch (source.getType()) {
                case MOVIE -> MovieManager.deleteAllMoviesInsideDir(eventPath);
            }
            return;
        }

        switch (source.getType()) {
            case MOVIE -> MovieManager.deleteMovieFile(eventPath);
        }
    }

    public static void handleModifyEvent(Source source, Path eventPath) {
        if (Files.isDirectory(eventPath)) {
            switch (source.getType()) {
                case MOVIE -> MovieManager.handleMovieFolderUpdate(eventPath, source);
            }
            return;
        }

        try (java.nio.channels.FileChannel ch = java.nio.channels.FileChannel.open(eventPath, java.nio.file.StandardOpenOption.WRITE);
             java.nio.channels.FileLock lock = ch.tryLock()) {

            if (lock != null) {
                lock.release();
                switch (source.getType()) {
                    case MOVIE -> MovieManager.processMovie(eventPath, source);
                }
            } else {
                // File is still being copied by the system/user.
                ServerApplication.LOGGER.info("File is still busy copying, skipping for now: " + eventPath.getFileName());
            }
        } catch (IOException e) {
            ServerApplication.LOGGER.debug("Cannot open file yet (likely locked during copy): " + eventPath.getFileName());
        }
    }


    public static void unregisterSource(String sourceId) {
        SourceWatcher sw = activeWatchers.remove(sourceId);
        if (sw != null) {
            sw.thread().interrupt(); // Stop the thread
            try {
                sw.service().close(); // Close the specific service
            } catch (IOException e) {
                ServerApplication.LOGGER.error("Error closing watcher", e);
            }
        }
    }
}