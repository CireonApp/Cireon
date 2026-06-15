package com.cireonapp.server.service;

import java.io.IOException;

public class FFmpegDownloadService {
    public static boolean downloadForWindows() {
        ProcessBuilder pb = new ProcessBuilder(
                "winget",
                "install",
                "--id","Gyan.FFmpeg",
                "--accept-source-agreements",
                "--accept-package-agreements",
                "--silent"
        );
        try {
            Process process = pb.start();

            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
