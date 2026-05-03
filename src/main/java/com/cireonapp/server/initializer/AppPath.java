package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppPath {

    public static final Path APP_DIR = resolveAppDir();

    private static Path resolveAppDir() {
        try {
            URI location = ServerApplication.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            Path codePath = Paths.get(location).toAbsolutePath().normalize();

            if (Files.isRegularFile(codePath)) {
                return codePath.getParent();
            }

            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        }
    }
}
