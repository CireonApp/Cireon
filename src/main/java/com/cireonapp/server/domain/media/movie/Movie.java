package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.CommonMedia;
import com.cireonapp.server.domain.media.common.VideoMediaFile;
import com.cireonapp.server.domain.media.source.SourceType;
import org.dizitart.no2.repository.annotations.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Movie extends CommonMedia {

    @Id
    private String id;
    private MovieMetadata metadata;
    private MovieMetadata overrides;
    private List<VideoMediaFile> files;
    private boolean hasFiles;

    /**
     * update movie metadata and overrides with non-null values from the source. This allows for partial updates to the movie's metadata and overrides without overwriting existing values with nulls.
     * This WILL NOT update tehe files of the movie. You should handle it seperatly.
     */
    public void merge(Movie source) {
        if (source == null) return;
        //update overrides and metadata internally in their own class.
        if (source.metadata != null) {
            this.metadata.merge(source.metadata);
        }
        if (source.overrides != null) {
            this.metadata.merge(source.overrides);
        }
    }

    public void addFile(VideoMediaFile file) {
        for (VideoMediaFile existingFile : this.files) {
            if (existingFile.getHash().equals(file.getHash())) {
                existingFile.merge(file);
                this.hasFiles = !this.files.isEmpty();
                return;
            }
        }
        this.files.add(file);
        this.hasFiles = !this.files.isEmpty();
    }

    public void removeFile(String hash) {
        this.files.removeIf(file -> file.getHash().equals(hash));
        this.hasFiles = !this.files.isEmpty();
    }


    public VideoMediaFile getFileByHash(String hash) {
        for (VideoMediaFile existingFile : this.files) {
            if (existingFile.getHash().equals(hash)) {
                return existingFile;
            }
        }
        return null;
    }

    public boolean doesFileExist(String hash) {
        for (VideoMediaFile existingFile : this.files) {
            if (existingFile.getHash().equals(hash)) {
                return true;
            }
        }
        return false;
    }

    public Movie() {
        super(SourceType.MOVIE);
        this.id = UUID.randomUUID().toString();
        this.files = new ArrayList<>();
        this.hasFiles = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MovieMetadata getOriginalMetadata() {
        return metadata;
    }

    public MovieMetadata getMetadata() {
        if (this.overrides == null) return this.metadata;
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

    public List<VideoMediaFile> getFiles() {
        return files;
    }

    public void setFiles(List<VideoMediaFile> files) {
        this.files = files;
    }


    public boolean isHasFiles() {
        return hasFiles;
    }

    public void setHasFiles(boolean hasFiles) {
        this.hasFiles = hasFiles;
    }
}
