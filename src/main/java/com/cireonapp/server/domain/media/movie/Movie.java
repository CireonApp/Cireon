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

    public Movie(String hash, Path filePath, MovieMetadata metadata) {
        super(SourceType.MOVIE);
        this.hash = hash;
        this.filePath = filePath;
        this.metadata = metadata;
    }

    public Movie(){
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

    public MovieMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MovieMetadata metadata) {
        this.metadata = metadata;
    }
}
