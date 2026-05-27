package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.common.CommonMetadata;
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
public class MovieMetadata extends CommonMetadata {
    private String releaseDate;


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
}
