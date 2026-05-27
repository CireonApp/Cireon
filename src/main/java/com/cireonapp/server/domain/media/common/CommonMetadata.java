package com.cireonapp.server.domain.media.common;

import org.dizitart.no2.repository.annotations.Id;

import java.util.HashSet;
import java.util.Set;

public class CommonMetadata {
    @Id
    private int id;
    private String title;
    private String originalTitle;
    private String description;
    private Artwork artworks;
    private int runtime;
    private Set<Genres> genres = new HashSet<>();
    private Set<String> alternativeTitles = new HashSet<>();
    private boolean adult;
    private String tagline;
    private long lastUpdated;
    private long releaseDateTimestamp;

    public CommonMetadata(int id, String title, String originalTitle, String description, Artwork artworks, int runtime, Set<Genres> genres, Set<String> alternativeTitles, boolean adult, String tagline, long lastUpdated) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.description = description;
        this.artworks = artworks;
        this.runtime = runtime;
        this.genres = genres;
        this.alternativeTitles = alternativeTitles;
        this.adult = adult;
        this.tagline = tagline;
        this.lastUpdated = lastUpdated;
    }


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

    @Override
    public String toString() {
        return "MovieMetadata{" +
                "title='" + title + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", description='" + description + '\'' +
                ", artworks=" + artworks +
                ", runtime=" + runtime +
                ", genres=" + genres +
                ", alternativeTitles=" + alternativeTitles +
                ", adult=" + adult +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getReleaseDateTimestamp() {
        return releaseDateTimestamp;
    }

    public void setReleaseDateTimestamp(long releaseDateTimestamp) {
        this.releaseDateTimestamp = releaseDateTimestamp;
    }

}
