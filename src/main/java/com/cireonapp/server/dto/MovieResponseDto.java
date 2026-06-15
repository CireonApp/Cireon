package com.cireonapp.server.dto;

import com.cireonapp.server.domain.media.common.Genres;
import com.cireonapp.server.domain.media.movie.Movie;

import java.util.HashSet;
import java.util.Set;

public class MovieResponseDto {
    private String id;
    private String title;
    private String originalTitle;
    private String description;
    private String releaseDate;
    private int runtime;
    private String[] genres;
    private Set<String> alternativeTitles = new HashSet<>();
    private boolean adult;
    private String tagline;

    MovieResponseDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public Set<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    public void setAlternativeTitles(Set<String> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public static MovieResponseDto getDtoFromMovie(Movie movie) {
        MovieResponseDto movieResponseDto = new MovieResponseDto();
        movieResponseDto.setId(movie.getId());
        movieResponseDto.setTitle(movie.getMetadata().getTitle());
        movieResponseDto.setOriginalTitle(movie.getMetadata().getOriginalTitle());
        movieResponseDto.setDescription(movie.getMetadata().getDescription());
        movieResponseDto.setReleaseDate(movie.getMetadata().getReleaseDate());
        movieResponseDto.setRuntime(movie.getMetadata().getRuntime());
        movieResponseDto.setGenres(Genres.GenresToStringArray(movie.getMetadata().getGenres()));

        movieResponseDto.setAlternativeTitles(movie.getMetadata().getAlternativeTitles());
        movieResponseDto.setAdult(movie.getMetadata().isAdult());
        movieResponseDto.setTagline(movie.getMetadata().getTagline());

        return movieResponseDto;
    }
}
