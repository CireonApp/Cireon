package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.Overrides;
import org.dizitart.no2.repository.annotations.Id;

import java.nio.file.Path;
import java.time.LocalDateTime;


public class Movie {
    /**
     * First and last 4mb of a file + file size.
     */
    @Id
    private String hash;
    private Path filePath;
    private MovieMetadata metadata;
    private LocalDateTime lastUpdated;
    private Overrides overrides;

    public Movie(String hash, Path filePath, MovieMetadata metadata) {
        this.hash = hash;
        this.filePath = filePath;
        this.metadata = metadata;
        lastUpdated = LocalDateTime.now();
    }

    public Movie(){
        lastUpdated = LocalDateTime.now();
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

    public MovieMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MovieMetadata metadata) {
        this.metadata = metadata;
    }


    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
