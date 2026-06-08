package com.cireonapp.server.domain.media.source;

public enum SourceType {
    MOVIE("Movies","movie"),
    TV_SHOW("Shows","show"),
    MUSIC("Music","music"),
    BOOK("Books","book");

    private final String label;
    private final String singular;

    public String getLabel() {
        return this.label;
    }

    public String getSingular() {
        return this.singular;
    }

    SourceType(String label, String singular) {
        this.label = label;
        this.singular = singular;
    }
}
