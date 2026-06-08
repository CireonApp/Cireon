package com.cireonapp.server.initializer;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppPath {
    public static final Path APP_DIR = init();

    private static Path init() {
        String override = System.getProperty("app.dir");

        Path path;

        if (override != null && !override.isBlank()) {
            path = Paths.get(override);
        } else {
            AppDirs appDirs = AppDirsFactory.getInstance();

            path = Paths.get(
                    appDirs.getUserDataDir(
                            "Cireon", // app name
                            null,            // version
                            "Cireon"         // author/company
                    )
            );
        }

        path = path.toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);

            Path probeFile = Files.createTempFile(path, ".rw-check-", ".tmp");
            try {
                Files.writeString(probeFile, "probe");
                Files.readString(probeFile);
            } finally {
                Files.deleteIfExists(probeFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "App directory is not readable/writable: " + path,
                    e
            );
        }

        return path;
    }
}
