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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @GetMapping(value = "/video/{hash}", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the requested content",
                    content = @Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User is not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The requested content was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )
    }
    )
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




}
