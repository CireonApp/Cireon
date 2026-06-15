package com.cireonapp.server;

import ch.qos.logback.classic.Level;
import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.initializer.AppPath;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.SourceSubscription;
import com.cireonapp.server.service.FFmpegServices;
import com.cireonapp.server.util.DataDirHelper;
import com.cireonapp.server.util.InternetConnection;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Cireon API",
                version = "1.0",
                description = "API for Cireon Media Server"
        )
)

@SpringBootApplication
public class ServerApplication {
    private static ConfigurableApplicationContext application;

    public static final Logger LOGGER =
            LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        try {
            FFmpegDownloadDialog();
            //why won't it shut up!!!!! even in prod!!
            silenceLogger("nitrite");
            silenceLogger("org.dizitart");

            DataDirHelper.initializeDataDir();
            Databases.bootstrap();
            Config config = ConfigManager.get();
            Databases.shutdown();

            unsilenceLogger("nitrite");
            unsilenceLogger("org.dizitart");

            SpringApplication app = new SpringApplication(ServerApplication.class);
            Databases.logInitialized = true;
            app.addInitializers(new Databases());

            // Only set dynamic runtime properties here, NOT the banner info
            Map<String, Object> props = new HashMap<>();
            props.put("server.port", config.getPort());
            app.setDefaultProperties(props);

            app.addInitializers(new SourceSubscription());
            application = app.run(args);

            LOGGER.info("Data folder location: {}", AppPath.APP_DIR);
            LOGGER.info("Server started on port {}", config.getPort());
        } catch (Throwable e) {
            String exceptionName = e.getClass().getName();

            // DevTools uses this exception internally to restart the app. do not print as an error.
            if (exceptionName.contains("SilentExitException")) {
                return;
            }

            e.printStackTrace();

            if (exceptionName.contains("PortInUseException")) {
                LOGGER.warn("Port is already in use!");
                shutdownApplication();
            }
        }
    }

    private static void silenceLogger(String name) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name)).setLevel(Level.OFF);
    }

    private static void unsilenceLogger(String name) {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name)).setLevel(Level.TRACE);
    }


    private static void FFmpegDownloadDialog() {
        // Step 1: Quick exit if already installed and working
        if (FFmpegServices.checkFFmpegInstallation()) {
            System.out.print("\n[INFO] FFmpeg and FFprobe are installed and working correctly.\n");
            return;
        }

        System.out.print("\n[ERROR] FFmpeg and/or FFprobe are not installed or not working correctly.");

        // Step 2: Guard against no internet connection
        if (!InternetConnection.isConnected()) {
            System.out.print("\n[ERROR] No internet connection. Cannot download FFmpeg. Please install FFmpeg manually and ensure it is in the system PATH.");
            System.out.print("\n[INFO] Tip: Launch Cireon again with internet connection to automatically download FFmpeg.");
            shutdownApplication();
            return;
        }

        // Step 3: Guard against a failed download/install attempt
        System.out.print("\n[INFO] Attempting to download FFmpeg...");
        if (!FFmpegServices.downloadFFmpeg()) {
            System.out.print("\n[ERROR] Failed to download FFmpeg. Please install FFmpeg manually and ensure it is in the system PATH.");
            if(SystemUtils.IS_OS_WINDOWS) {
                System.out.print("\n[INFO] Try the following command ind CMD or PowerShell: winget install ffmpeg");
            }
            shutdownApplication();
            return;
        }

        System.out.print("\n[INFO] FFmpeg downloaded successfully. Checking installation...");

        // Step 4: Final verification check
        if (FFmpegServices.checkFFmpegInstallation()) {
            System.out.print("\n[INFO] FFmpeg is now installed and working correctly.\n");
        } else {
            System.out.print("\n[ERROR] FFmpeg was downloaded but is still not working correctly. Please install FFmpeg manually and ensure it is in the system PATH.");
            shutdownApplication();
        }
    }

    private static void shutdownApplication() {
        application.close();
        System.out.print("\nPress Enter to close...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }

}
