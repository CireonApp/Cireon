package com.cireonapp.server.domain.media.common;

import org.dizitart.no2.repository.annotations.Id;

import java.util.HashSet;
import java.util.Set;

public class CommonMetadata {
    @Id
    private String id;
    private String title;
    private String originalTitle;
    private String description;
    private Artwork artworks;
    private Integer runtime;
    private Set<Genres> genres = new HashSet<>();
    private Set<String> alternativeTitles = new HashSet<>();
    private Boolean adult;
    private String tagline;
    private Long lastUpdated;
    private Long releaseDateTimestamp;

    public CommonMetadata() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Artwork getArtworks() {
        return artworks;
    }

    public void setArtworks(Artwork artworks) {
        this.artworks = artworks;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public Set<Genres> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genres> genres) {
        this.genres = genres;
    }

    public Set<String> getAlternativeTitles() {
        return alternativeTitles;
    }

    public void setAlternativeTitles(Set<String> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getReleaseDateTimestamp() {
        return releaseDateTimestamp;
    }

    public void setReleaseDateTimestamp(long releaseDateTimestamp) {
        this.releaseDateTimestamp = releaseDateTimestamp;
    }

}
