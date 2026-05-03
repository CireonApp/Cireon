package com.cireonapp.server.domain.media.common;

import java.nio.file.Path;

public class Artwork {

    /**
     * Background image for the media item. This is typically a large image that can be used as a backdrop in the UI.
     */
    private String background;
    /**
     * Clear logo image for the media item. This is a transparent logo that can be overlaid on top of the background or used in other UI elements.
     */
    private String logo;
    /**
     * Poster image for the media item. This is typically a vertical image that can be used in lists, grids, or as a thumbnail for the media item.
     */
    private String poster;

    public Artwork(String background, String logo, String poster) {
        this.background = background;
        this.logo = logo;
        this.poster = poster;
    }

    public Artwork() {
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
}
