package com.cireonapp.server.domain.media.source;

public enum SourceType {
    MOVIE("movie"),
    TV_SHOW("tv_show"),
    MUSIC("music"),
    BOOK("book");

    private final String label;

    public String getLabel() {
        return label;
    }

    SourceType(String label) {
        this.label = label;
    }
}
