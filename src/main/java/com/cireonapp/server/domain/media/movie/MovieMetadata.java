package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.common.Genres;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

import java.util.HashSet;
import java.util.Set;


@Entity(value = "movieMetadata", indices = {
        @Index(fields = "title", type = IndexType.FULL_TEXT),
})
public class MovieMetadata {
    @Id
    private int id;
    private String title;
    private String originalTitle;
    private String description;
    private String releaseDate;
    private Artwork artworks;
    private int runtime;
    private Set<Genres> genres = new HashSet<>();
    private Set<String> alternativeTitles = new HashSet<>();
    private boolean adult;
    private String tagline;
    private long lastUpdated;
    private long releaseDateTimestamp;

    public MovieMetadata(int id, String title, String originalTitle, String description, String releaseDate, Artwork artworks, int runtime, Set<Genres> genres, Set<String> alternativeTitles, boolean adult, String tagline, long lastUpdated) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.description = description;
        this.releaseDate = releaseDate;
        this.artworks = artworks;
        this.runtime = runtime;
        this.genres = genres;
        this.alternativeTitles = alternativeTitles;
        this.adult = adult;
        this.tagline = tagline;
        this.lastUpdated = lastUpdated;
    }

    public MovieMetadata() {
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

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
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
                ", releaseDate='" + releaseDate + '\'' +
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
