package com.cireonapp.server.domain.media.movie;

import org.dizitart.no2.repository.annotations.Id;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class Movie {
    /**
     * First and last 4mb of a file + file size.
     */
    @Id
    private String hash;
    private Path filePath;
    private MovieMetadata metadata;
    private long lastUpdated;
    private MovieMetadata overrides;
    private long created;

    public Movie(String hash, Path filePath, MovieMetadata metadata) {
        this.hash = hash;
        this.filePath = filePath;
        this.metadata = metadata;
        lastUpdated = Timestamp.valueOf(LocalDateTime.now()).getTime();
        created = Timestamp.valueOf(LocalDateTime.now()).getTime();
    }

    public Movie(){
        lastUpdated = Timestamp.valueOf(LocalDateTime.now()).getTime();
        created = Timestamp.valueOf(LocalDateTime.now()).getTime();
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


    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated() {
        this.lastUpdated = Timestamp.valueOf(LocalDateTime.now()).getTime();
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
