package com.cireonapp.server.util;

import java.nio.file.Path;

import static com.cireonapp.server.initializer.AppPath.APP_DIR;

public class DataDirHelper {
    public static Path getDataDir() {
        return APP_DIR.resolve("data");
    }

    public static void initializeDataDir() {
        Path dataDir = getDataDir();
        if (!dataDir.toFile().exists()) {
            dataDir.toFile().mkdirs();
        }

        Path contentDir = dataDir.resolve("content");
        if (!contentDir.toFile().exists()) {
            contentDir.toFile().mkdirs();
        }

        Path pluginsDir = dataDir.resolve("plugins");
        if (!pluginsDir.toFile().exists()) {
            pluginsDir.toFile().mkdirs();
        }
    }
}
