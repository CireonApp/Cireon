package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.service.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;


@RestController
@RequestMapping("/api/streaming")
public class ContentController {
    @Autowired
    private StreamingService service;

    @GetMapping(value = "/video/{id}", produces = "video/mp4")
    public Mono<Resource> getVideo(String id) {

        //get actual video from database using the id. For testing purposes, we will just return a hardcoded video path.
        String videoPath = "C:/Users/tzurs/Downloads/big buck bunny (2008).mp4";

        return service.getVideo(videoPath);
    }

    @GetMapping(value = "/test")
    public ResponseEntity<?> test(String id) {
//        Source source = new Source();
//        source.setType(SourceType.MOVIE);
//        ExternalMetadataKeys keys = new ExternalMetadataKeys();
//        keys.setMovieDB("eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyMjE0M2YyMWNkNjRhOThmNWI3OTRmZDg0OTM3MDU3ZiIsIm5iZiI6MTY1MTk5MTg4MC4xNjIsInN1YiI6IjYyNzc2NTQ4ZTdjMDk3MDBhNjFmNDg2MSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.zhO6Dlb4Xq2LBwL-vmqFtC89EvW_yA7KtifrsgZ4FpY");
//        source.setExternalMetadataKeys(keys);
//        source.setDirPath(Paths.get("C:/Users/tzurs/Downloads"));
//        FileWatcher.RegisterPath(source);
        MovieManager.search("arhjareyrewt").forEach(movie -> System.out.println(movie.getMetadata().getTitle()));
        return ResponseEntity.ok("Test successful! id: " + id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponseDto("The requested content was not found!"));
    }

}
