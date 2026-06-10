package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/streaming")
public class StreamingController {

    private static final int BUFFER_SIZE = 65536;
    private static final String FFPROBE_PATH = "C:\\Users\\tzurs\\Downloads\\ffmpeg\\bin\\ffprobe.exe";
    private static final String FFMPEG_PATH = "C:\\Users\\tzurs\\Downloads\\ffmpeg\\bin\\ffmpeg.exe";
    private static String cachedVideoCodec = null;

    // TRACKER: Keep track of only one process per video
    private static final Map<String, Process> activeStreams = new ConcurrentHashMap<>();
    private static final Map<String, Long> cachedDuration = new ConcurrentHashMap<>();

    // 1. Add this to your StreamingController
    @GetMapping("/info/{hash}")
    public ResponseEntity<Map<String, String>> getMovieInfo(@PathVariable String hash) {
        Optional<Movie> movie = MovieManager.get(hash);
        if (movie.isEmpty()) return ResponseEntity.notFound().build();

        // Use cached duration if available
        if (cachedDuration.containsKey(hash)) {
            return ResponseEntity.ok(Map.of("duration", String.valueOf(cachedDuration.get(hash))));
        }

        try {
            // Use ffprobe for metadata, it's MUCH faster and lighter than ffmpeg
            Process p = new ProcessBuilder(
                    FFPROBE_PATH,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    movie.get().getFilePath().toString()
            ).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

                String line = reader.readLine();
                if (line != null) {
                    long duration = Math.round(Double.parseDouble(line));
                    cachedDuration.put(hash, duration);
                    return ResponseEntity.ok(Map.of("duration", String.valueOf(duration)));
                } else {
                    // Read error stream if output is empty
                    String error = errorReader.lines().collect(Collectors.joining("\n"));
                    System.err.println("FFPROBE ERROR: " + error);
                }
            }
        } catch (Exception e) {
            System.err.println("CRITICAL PROBE EXCEPTION: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping(value = "/video/{hash}", produces = "video/mp4")
    public ResponseEntity<StreamingResponseBody> streamOnTheFly(
            @PathVariable String hash,
            @RequestParam(defaultValue = "0") int start) {

        Optional<Movie> movie = MovieManager.get(hash);
        if (movie.isEmpty()) return ResponseEntity.notFound().build();

        String inputPath = movie.get().getFilePath().toString();

        // 2. CLEANUP: If this video is already streaming, kill the old process
        if (activeStreams.containsKey(hash)) {
            Process oldProcess = activeStreams.get(hash);
            if (oldProcess != null && oldProcess.isAlive()) {
                oldProcess.destroyForcibly();
            }
            activeStreams.remove(hash);
        }

        if (cachedVideoCodec == null) cachedVideoCodec = resolveBestCodec();
        String preset = resolvePreset(cachedVideoCodec);

        StreamingResponseBody responseBody = outputStream -> {
            Process process = null;
            try {
                process = new ProcessBuilder(
                        FFMPEG_PATH,
                        "-loglevel", "error",
                        "-ss", String.valueOf(start),
                        "-i", inputPath,
                        "-threads", "0",
                        "-vcodec", cachedVideoCodec,
                        "-preset", preset,
                        "-acodec", "aac",
                        "-ac", "2",
                        "-f", "mp4",
                        "-movflags", "frag_keyframe+empty_moov+faststart+default_base_moof",
                        "-"
                ).start();

                activeStreams.put(hash, process);

                try (InputStream ffmpegOut = process.getInputStream()) {
                    byte[] buffer = new byte[65536]; // 64KB buffer
                    int bytesRead;
                    while ((bytesRead = ffmpegOut.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush();
                    }
                }
            } catch (Exception e) {
                // Silently ignore browser-induced disconnects
            } finally {
                activeStreams.remove(hash);
                if (process != null) process.destroyForcibly();
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s.mp4\"", movie.get().getHash()))
                .body(responseBody);
    }

    private String resolveBestCodec() {
        // Define the priority order
        String[] priorityList = {"h264_nvenc", "h264_qsv", "h264_amf"};

        try {
            // Get the list of all supported encoders
            Process p = new ProcessBuilder(FFMPEG_PATH, "-encoders").start();
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            // Loop through our list of hardware codecs
            for (String codec : priorityList) {
                // Only attempt if the encoder is present AND passes the dry-run test
                if (output.contains(codec)) {
                    System.out.println("Testing hardware encoder: " + codec);
                    if (canInitialize(codec)) {
                        System.out.println("Success! Using hardware encoder: " + codec);
                        return codec;
                    } else {
                        System.err.println("Hardware encoder " + codec + " present but failed initialization. Skipping.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during hardware detection: " + e.getMessage());
        }

        System.out.println("No functional hardware encoders found. Falling back to software (libx264).");
        return "libx264";
    }

    // A simple dry-run to ensure the codec actually works
    private boolean canInitialize(String codec) {
        try {
            // Run a dummy transcode of 1 frame to see if it crashes
            Process p = new ProcessBuilder(FFMPEG_PATH,
                    "-f", "lavfi", "-i", "testsrc=duration=0.1:size=320x240:rate=1",
                    "-vcodec", codec, "-f", "null", "-").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String resolvePreset(String codec) {
        return switch (codec) {
            case "h264_nvenc" -> "p4";
            case "h264_qsv" -> "veryfast";
            case "h264_amf" -> "balanced";
            default -> "ultrafast"; // libx264
        };
    }
}