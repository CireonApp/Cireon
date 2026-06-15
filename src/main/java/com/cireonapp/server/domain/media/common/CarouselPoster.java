package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.domain.media.movie.Movie;

public class CarouselPoster {
    private String hash;
    private String title;
    private long creationDate;
    private long updateDate;
    private boolean adult;

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    private String releaseDate;

    public static CarouselPoster createCarouselPosterFromMovie(Movie movie) {
        CarouselPoster poster = new CarouselPoster();
        poster.setAdult(movie.getMetadata().isAdult());
        poster.setHash(movie.getId());
        poster.setCreationDate(movie.getCreated());
        poster.setReleaseDate(movie.getMetadata().getReleaseDate());
        poster.setUpdateDate(movie.getLastUpdated());
        poster.setTitle(movie.getMetadata().getTitle());
        return poster;
    }
}
