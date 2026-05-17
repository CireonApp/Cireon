package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppPath {

    public static final Path APP_DIR = init();

    private AppPath() {}

    private static Path init() {
        String override = System.getProperty("app.dir");

        Path path;

        if (override != null && !override.isBlank()) {
            path = Paths.get(override);
        } else {
            AppDirs appDirs = AppDirsFactory.getInstance();

            path = Paths.get(
                    appDirs.getUserDataDir(
                            "CireonBackend", // app name
                            null,            // version
                            "Cireon"         // author/company
                    )
            );
        }

        path = path.toAbsolutePath().normalize();

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to create app directory: " + path,
                    e
            );
        }

        return path;
    }
}
