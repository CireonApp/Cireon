package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.common.SearchResults;
import com.cireonapp.server.domain.media.movie.Movie;
import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.dto.MovieResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    @GetMapping("/search")
    public ResponseEntity<List<SearchResults<MovieResponseDto>>> getAll(@RequestParam(value = "query",defaultValue = "") String query) {
        List<SearchResults<Movie>> searchResults = MovieManager.search(query, 15);
        List<SearchResults<MovieResponseDto>> results = new ArrayList<>();
        searchResults.forEach(result -> {
            SearchResults<MovieResponseDto> converted = new SearchResults<>(MovieResponseDto.getDtoFromMovie(result.content()), result.score());
            results.add(converted);
        });
        return ResponseEntity.ok(results);
    }
}
