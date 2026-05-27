package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.common.SearchResults;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.MovieResponseDto;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Tag(name = "Movies API", description = "Movies related endpoints")
@RestController
@RequestMapping("/api/movie")
public class MovieController {
    @Operation(
            summary = "Simple movie Search.",
            description = "Search movies by title. Returns a list of movies that match the query with scoring."
    )
    @GetMapping("/search")
    public ResponseEntity<?> getAll(HttpServletRequest request, @RequestParam(value = "query", defaultValue = "") String query) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        List<SearchResults<Movie>> searchResults = MovieManager.search(query, 15);
        List<SearchResults<MovieResponseDto>> results = new ArrayList<>();
        searchResults.forEach(result -> {
            SearchResults<MovieResponseDto> converted = new SearchResults<>(MovieResponseDto.getDtoFromMovie(result.content()), result.score());
            results.add(converted);
        });
        return ResponseEntity.ok(results);
    }
}
