package com.cireonapp.server.domain.media.watchProgress;

import org.dizitart.no2.repository.annotations.Id;

import java.util.HashMap;
import java.util.Map;

public class UserVideoWatchProgress {
    @Id
    private String username;
    private final Map<String,Integer> media;

    public UserVideoWatchProgress() {
        media = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Integer> getMedia() {
        return media;
    }

    public void updateMedia(String id, Integer progress) {
        media.put(id, progress);
    }
}
