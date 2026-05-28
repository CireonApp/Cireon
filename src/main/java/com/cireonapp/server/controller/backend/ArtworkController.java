package com.cireonapp.server.controller.backend;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.initializer.AppPath;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.Optional;

@Tag(name = "Artwork API", description = "Endpoints for fetching media artwork. Requires authentication.")
@RestController
public class ArtworkController {
    private static final String APP_ICON_RESOURCE = "static/assets/icons/cireon_nobackground.svg";

    public enum ArtworkType {
        background,
        poster,
        logo
    }

    @Autowired
    private ContentService service;


    @Operation(
            summary = "Fetch media artwork.",
            description = "Fetch media artwork. Requires authentication. Returns the requested artwork as a byte stream."
    )
    @GetMapping(value = "/api/artwork", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
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
                    responseCode = "404",
                    description = "Not found - artwork was not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
    }
    )
    public Mono<Resource> getVideo(@RequestParam(value = "type") ArtworkType type, @RequestParam(value = "id") String id, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty()) {
            return Mono.error(new IllegalArgumentException(CommonResponseDto.Error.NOT_LOGGED_IN.errorMessage));
        }


        Artwork artwork;
        String imagePath = "";

        SourceType sourceType = GuessMediaType.guess(id);

        switch (sourceType) {
            case SourceType.MOVIE:
                Optional<Movie> movie = MovieManager.get(id);
                if (movie.isEmpty()) return service.getClasspathContent(APP_ICON_RESOURCE);
                artwork = movie.get().getMetadata().getArtworks();
                break;

            case null, default:
                return Mono.error(new FileNotFoundException("The requested content was not found!"));
        }

        if (artwork == null) {
            return service.getClasspathContent(APP_ICON_RESOURCE);
        }

        switch (type) {
            case background:
                String background = artwork.getBackground();
                if (background == null || background.isBlank()) {
                    return service.getClasspathContent(APP_ICON_RESOURCE);
                }
                imagePath = AppPath.APP_DIR.resolve("data/content").resolve(background).toString();
                break;
            case poster:
                String poster = artwork.getPoster();
                if (poster == null || poster.isBlank()) {
                    return service.getClasspathContent(APP_ICON_RESOURCE);
                }
                imagePath = AppPath.APP_DIR.resolve("data/content").resolve(poster).toString();
                break;
            case logo:
                String logo = artwork.getLogo();
                if (logo == null || logo.isBlank()) {
                    return service.getClasspathContent(APP_ICON_RESOURCE);
                }
                imagePath = AppPath.APP_DIR.resolve("data/content").resolve(logo).toString();
                break;
        }

        if (imagePath.isBlank()) {
            return service.getClasspathContent(APP_ICON_RESOURCE);
        }

        return service.getContent(imagePath)
                .onErrorResume(FileNotFoundException.class, error -> Mono.error(new FileNotFoundException("The requested content was not found!")));
    }


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto(ex.getMessage()));
    }
}
