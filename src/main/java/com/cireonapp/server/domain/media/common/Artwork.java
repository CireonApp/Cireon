package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.initializer.AppPath;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

    private static boolean imageExists(String hash, String type, String ext) {
        Path out = AppPath.APP_DIR.resolve("data/content/" + hash + "_" + type + "." + ext);
        return out.toFile().exists();
    }

    public static String saveImage(String url, String hash, String type, String ext) {
        try {
            Path out = AppPath.APP_DIR.resolve("data/content/" + hash + "_" + type + "." + ext);
            if (imageExists(hash, type, ext)){
                ServerApplication.LOGGER.info("Image already exists");
                return out.getFileName().toString();
            };
            InputStream in = new URL(url).openStream();
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            return out.getFileName().toString();
        } catch (Exception e) {
            return null;
        }
    }
}
