package com.cireonapp.server.controller.backend;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;
import com.cireonapp.server.initializer.AppPath;
import com.cireonapp.server.service.ContentService;
import com.cireonapp.server.util.GuessMediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.util.Optional;

@RestController
public class ArtworkController {
    private static final String APP_ICON_RESOURCE = "static/assets/icons/cireon_nobackground.svg";

    @Autowired
    private ContentService service;

    @GetMapping(value = "/api/artwork", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> getVideo(@RequestParam(value = "type") String type, @RequestParam(value = "id") String id) {
        ServerApplication.LOGGER.info("test");
        Artwork artwork = null;
        String imagePath = "";

        SourceType sourceType = GuessMediaType.guess(id);

        switch (sourceType) {
            case SourceType.MOVIE:
                Optional<Movie> movie = MovieManager.get(id);
                if (movie.isEmpty()) return service.getClasspathContent(APP_ICON_RESOURCE);
                artwork = movie.get().getMetadata().getArtworks();
                break;

            case null, default:
                return Mono.empty();
        }

        if (artwork == null) {
            return service.getClasspathContent(APP_ICON_RESOURCE);
        }

        switch (type) {
            case "background":
                String background = artwork.getBackground();
                if (background == null || background.isBlank()) {
                    return service.getClasspathContent(APP_ICON_RESOURCE);
                }
                imagePath = AppPath.APP_DIR.resolve("data/content").resolve(background).toString();
                break;
            case "poster":
                String poster = artwork.getPoster();
                if (poster == null || poster.isBlank()) {
                    return service.getClasspathContent(APP_ICON_RESOURCE);
                }
                imagePath = AppPath.APP_DIR.resolve("data/content").resolve(poster).toString();
                break;
            case "logo":
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
                .onErrorResume(FileNotFoundException.class, error -> service.getClasspathContent(APP_ICON_RESOURCE));
    }
}
