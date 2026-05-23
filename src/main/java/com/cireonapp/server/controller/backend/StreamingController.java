package com.cireonapp.server.controller.backend;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.common.ExternalMetadataSources;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.ExternalMetadataKeys;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.initializer.FileWatcher;
import com.cireonapp.server.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Optional;


@RestController
@RequestMapping("/api/streaming")
public class StreamingController {
    @Autowired
    private ContentService service;

    @GetMapping(value = "/video/{id}",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> getVideo(@PathVariable String id) {

        ServerApplication.LOGGER.info("test");

        Optional<Movie> movie = MovieManager.get(id);


        String videoPath = movie.get().getFilePath().toString();

        return service.getContent(videoPath);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponseDto("The requested content was not found!"));
    }

}
