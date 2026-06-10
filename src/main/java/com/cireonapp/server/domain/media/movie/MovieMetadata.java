package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.common.CommonMetadata;
import com.cireonapp.server.domain.media.common.Genres;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.index.IndexType;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Index;

import java.util.Set;


@Entity(value = "movieMetadata", indices = {
        @Index(fields = "title", type = IndexType.FULL_TEXT),
})
public class MovieMetadata extends CommonMetadata {
    private String releaseDate;

    public void merge(MovieMetadata newData) {
        if (newData == null) return;
        //id should not change.
        if (newData.getTitle() != null)
            this.setTitle(newData.getTitle());
        if (newData.getOriginalTitle() != null)
            this.setOriginalTitle(newData.getOriginalTitle());
        if (newData.getDescription() != null)
            this.setDescription(newData.getDescription());
        if (newData.getTagline() != null)
            this.setTagline(newData.getTagline());
        if (newData.getReleaseDate() != null)
            this.setReleaseDate(newData.getReleaseDate());
        if (newData.getGenres() != null)
            this.setGenres(newData.getGenres());
        if (newData.getArtworks() != null)
            this.setArtworks(newData.getArtworks());
        if (newData.getAlternativeTitles() != null)
            this.setAlternativeTitles(newData.getAlternativeTitles());
        if (newData.getTagline() != null)
            this.setTagline(newData.getTagline());
    }


    public MovieMetadata() {
        super();
    }

    public MovieMetadata(int id, String title, String originalTitle, String description, String releaseDate, Artwork artworks, int runtime, Set<Genres> genres, Set<String> alternativeTitles, boolean adult, String tagline, long lastUpdated) {
        super(id, title, originalTitle, description, artworks, runtime, genres, alternativeTitles, adult, tagline, lastUpdated);
        this.releaseDate = releaseDate;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public MovieMetadata clone() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(this), MovieMetadata.class);
        } catch (JsonProcessingException e) {
            return this;
        }
    }
}
