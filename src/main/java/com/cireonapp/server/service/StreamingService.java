package com.cireonapp.server.service;

import com.cireonapp.server.ServerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class StreamingService {
    @Autowired
    private ResourceLoader resourceLoader;

    public Mono<Resource> getVideo(String videoPath) {
        Path path = Path.of(videoPath);

        if (!Files.exists(path) || !Files.isReadable(path)) {
            ServerApplication.LOGGER.warning("File not found: " + videoPath);
            return Mono.error(new FileNotFoundException("File not found: " + videoPath));
        }

        return Mono.fromSupplier(()->resourceLoader.getResource(path.toUri().toString()));
    }
}
