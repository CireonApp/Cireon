package com.cireonapp.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class ContentService {
    @Autowired
    private ResourceLoader resourceLoader;

    public Mono<Resource> getContent(String contentPath) {
        Path path = Path.of(contentPath);

        if (!Files.exists(path) || !Files.isReadable(path)) {
            return Mono.error(new FileNotFoundException("File not found: " + contentPath));
        }

        return Mono.fromSupplier(()->resourceLoader.getResource(path.toUri().toString()));
    }

    public Mono<Resource> getClasspathContent(String resourcePath) {
        return Mono.fromSupplier(() -> resourceLoader.getResource("classpath:" + resourcePath));
    }
}
