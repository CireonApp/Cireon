package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.CommonMedia;
import com.cireonapp.server.domain.media.source.SourceType;
import org.dizitart.no2.repository.annotations.Id;

import java.nio.file.Path;


public class Movie extends CommonMedia {
    /**
     * First and last 4mb of a file + file size.
     */
    @Id
    private String hash;
    private Path filePath;
    private MovieMetadata metadata;
    private MovieMetadata overrides;

    public void merge(Movie newData) {
        if(newData == null) return;
        // hash should not be possible to change.
        if (newData.filePath != null) {
            this.filePath = newData.filePath;
        }
        //update overrides and metadata internally in their own class.
        if (newData.metadata != null) {
            this.metadata.merge(newData.metadata);
        }
        if (newData.overrides != null) {
            this.metadata.merge(newData.overrides);
        }
    }

    public Movie(String hash, Path filePath, MovieMetadata metadata) {
        super(SourceType.MOVIE);
        this.hash = hash;
        this.filePath = filePath;
        this.metadata = metadata;
    }

    public Movie() {
        super(SourceType.MOVIE);
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public MovieMetadata getOriginalMetadata() {
        //merge with overrides to get the metadata that is overriden by the user instead of fetched data.
        return metadata;
    }

    public MovieMetadata getMetadata() {
       if(this.overrides == null) return this.metadata;
        MovieMetadata clone = this.metadata.clone();
        clone.merge(this.overrides);
        return clone;
    }

    public void setMetadata(MovieMetadata metadata) {
        this.metadata = metadata;
    }

    public MovieMetadata getOverrides() {
        return overrides;
    }

    public void setOverrides(MovieMetadata overrides) {
        this.overrides = overrides;
    }
}
