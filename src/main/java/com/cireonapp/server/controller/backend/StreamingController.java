package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.common.MediaInfoByFFmpeg;
import com.cireonapp.server.domain.media.common.VideoMediaFile;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.service.FFmpegServices;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/streaming")
public class StreamingController {

    private static final double TARGET_CHUNK_DURATION = 12.0;

    private double getExactChunkDuration(double fps) {
        if (fps <= 0) return TARGET_CHUNK_DURATION;
        long framesPerChunk = (long) Math.ceil(TARGET_CHUNK_DURATION * fps);

        return (double) framesPerChunk / fps;
    }


    @GetMapping(value = "/hls/{hash}/playlist.m3u8", produces = "application/vnd.apple.mpegurl")
    public ResponseEntity<String> getVirtualPlaylist(@PathVariable String hash) {
        VideoMediaFile file = MovieManager.getFileByHash(hash);
        if (file == null) return ResponseEntity.notFound().build();

        Double duration = file.getDuration();
        Double fps = file.getFps();

        if (duration == null || fps == null) {
            MediaInfoByFFmpeg info = FFmpegServices.getVideoInfo(file.getFilePath().toString());
            if(info == null) return ResponseEntity.notFound().build();
            duration = info.getDuration();
            fps = info.getFps();
        }

        double exactChunkDuration = getExactChunkDuration(fps);
        int totalChunks = (int) Math.ceil(duration / exactChunkDuration);

        StringBuilder m3u8 = new StringBuilder();
        m3u8.append("#EXTM3U\n");
        m3u8.append("#EXT-X-VERSION:3\n");

        int targetDuration = (int) Math.ceil(exactChunkDuration) + 1;
        m3u8.append("#EXT-X-TARGETDURATION:").append(targetDuration).append("\n");
        m3u8.append("#EXT-X-MEDIA-SEQUENCE:0\n");
        m3u8.append("#EXT-X-PLAYLIST-TYPE:VOD\n");

        for (int i = 0; i < totalChunks; i++) {
            double currentChunkDuration = (i == totalChunks - 1)
                    ? duration - (i * exactChunkDuration)
                    : exactChunkDuration;

            m3u8.append(String.format(Locale.US, "#EXTINF:%.6f,\n", currentChunkDuration));
            m3u8.append("chunk_").append(i).append(".ts\n");
        }

        m3u8.append("#EXT-X-ENDLIST\n");
        return ResponseEntity.ok(m3u8.toString());
    }


    @GetMapping(value = "/hls/{hash}/chunk_{index}.ts", produces = "video/MP2T")
    public ResponseEntity<StreamingResponseBody> serveChunk(@PathVariable String hash, @PathVariable int index) {
        VideoMediaFile file = MovieManager.getFileByHash(hash);
        if (file == null) return ResponseEntity.notFound().build();

        String inputPath = file.getFilePath().toString();

        Double fps = file.getFps();
        if (fps == null || fps <= 0) fps = 24.0;

        double exactChunkDuration = getExactChunkDuration(fps);
        double startTime = index * exactChunkDuration;

        String preset = FFmpegServices.resolvePresetForCodec();
        List<String> rateControl = FFmpegServices.resolveRateControlParameter();

        StreamingResponseBody responseBody = outputStream -> {
            Process process = null;
            try {
                List<String> command = new ArrayList<>(Arrays.asList(
                        "ffmpeg",
                        "-loglevel", "error",
                        "-ss", String.format(Locale.US, "%.6f", startTime),
                        "-t", String.format(Locale.US, "%.6f", exactChunkDuration),
                        "-i", inputPath,
                        "-copyts",
                        "-map", "0:v:0",
                        "-map", "0:a:0",
                        "-sn",
                        "-threads", "0",
                        "-map_metadata", "-1",
                        "-map_chapters", "-1",
                        "-c:v", FFmpegServices.getEncoder().getLabel(),
                        "-preset", preset));

                command.addAll(rateControl);

                command.addAll(Arrays.asList(
                        "-flags", "+cgop",
                        "-sc_threshold", "0",
                        "-c:a", "aac",
                        "-b:a", "256k",
                        "-af", "aresample=async=1:min_hard_comp=0.010",
                        "-bsf:a", "aac_adtstoasc",
                        "-pix_fmt", "yuv420p",
                        "-max_muxing_queue_size", "2048",
                        "-avoid_negative_ts", "disabled",
                        "-muxdelay", "0",
                        "-muxpreload", "0",
                        "-f", "mpegts",
                        "pipe:1"
                ));

                process = new ProcessBuilder(command).start();

                try (InputStream ffmpegOut = process.getInputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = ffmpegOut.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            } catch (Exception ignored) {
            } finally {
                if (process != null) process.destroyForcibly();
            }
        };


        return ResponseEntity.ok()
                // Prevent browsers from caching generated chunks so client hard drives don't fill up
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(responseBody);
    }
}