package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppPath {

    public static final Path APP_DIR = resolveAppDir();

    private static Path resolveAppDir() {
        // Allow explicit override via system property (e.g. set by jpackage or tests)
        String override = System.getProperty("app.dir");
        if (override != null) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        try {
            URI location = ServerApplication.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            Path codePath = Paths.get(location).toAbsolutePath().normalize();
            Path candidate = Files.isRegularFile(codePath)
                    ? codePath.getParent()
                    : Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

            // If the candidate directory is not writable (e.g. C:\Program Files when installed),
            // fall back to the OS-appropriate user data directory.
            if (!Files.isWritable(candidate)) {
                return getOsDataDir();
            }

            return candidate;
        } catch (Exception e) {
            return getOsDataDir();
        }
    }

    private static Path getOsDataDir() {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, "CireonBackend");
            }
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "CireonBackend");
        } else {
            // Linux / other: respect XDG_DATA_HOME
            String xdg = System.getenv("XDG_DATA_HOME");
            if (xdg != null) {
                return Paths.get(xdg, "CireonBackend");
            }
            return Paths.get(System.getProperty("user.home"), ".local", "share", "CireonBackend");
        }

        // Final fallback
        return Paths.get(System.getProperty("user.home"), "CireonBackend");
    }
}
