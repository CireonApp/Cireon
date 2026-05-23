package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.domain.media.source.SourceType;

import java.util.ArrayList;
import java.util.List;

public class Carousel {
    private List<CarouselPoster> posters =  new ArrayList<>();
    private SourceType sourceType;
    private String title;
    private String description;

    public Carousel() {
    }

    public List<CarouselPoster> getPosters() {
        return posters;
    }

    public void setPosters(List<CarouselPoster> posters) {
        this.posters = posters;
    }

    public void addPoster(CarouselPoster poster) {
        this.posters.add(poster);
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
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
}
