package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.service.ContentService;
import com.cireonapp.server.util.CookieHelper;
import com.cireonapp.server.util.GuessMediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.Optional;

@Tag(name = "Streaming API", description = "Content streaming related endpoints")
@RestController
@RequestMapping("/api/streaming")
public class StreamingController {
    @Autowired
    private ContentService service;

    @Operation(
            summary = "Stream video content. Dont try here!",
            description = "Stream video content by hash. Requires valid authentication session."
    )
    @GetMapping(value = "/video/{hash}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> getVideo(@PathVariable String hash, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty()) {
            return Mono.error(new IllegalArgumentException(CommonResponseDto.Error.NOT_LOGGED_IN.errorMessage));
        }

        SourceType sourceType = GuessMediaType.guess(hash);
        if (sourceType == null) {
            return Mono.error(new FileNotFoundException("The requested content was not found!"));
        }

        if (sourceType == SourceType.MOVIE) {
            Optional<Movie> movie = MovieManager.get(hash);
            if (movie.isEmpty()) {
                return Mono.error(new FileNotFoundException("The requested content was not found!"));
            }
            String videoPath = movie.get().getFilePath().toString();
            return service.getContent(videoPath);
        }
        return Mono.error(new FileNotFoundException("Unsupported content type"));

    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponseDto(ex.getMessage() != null ? ex.getMessage() : "The requested content was not found!"));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(401).body(new ErrorResponseDto(ex.getMessage()));
    }

}
