package com.cireonapp.server.util;

import java.nio.file.Path;

import static com.cireonapp.server.initializer.AppPath.APP_DIR;

public class DataDirHelper {
    public static Path getDataDir() {
        return APP_DIR.resolve("data");
    }

    public static Path getPluginsDir() {
        return APP_DIR.resolve("plugins");
    }

    public static void initializeDataDir() {
        Path dataDir = getDataDir();
        Path pluginsDir = getPluginsDir();
        if (!dataDir.toFile().exists()) {
            dataDir.toFile().mkdirs();
        }

        Path contentDir = dataDir.resolve("content");
        if (!contentDir.toFile().exists()) {
            contentDir.toFile().mkdirs();
        }

        if (!pluginsDir.toFile().exists()) {
            pluginsDir.toFile().mkdirs();
        }
    }
}
