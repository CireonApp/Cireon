package com.cireonapp.server.util;

import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;

public class GuessMediaType {
    public static SourceType guess(String id) {
        if (MovieManager.get(id) != null)
            return SourceType.MOVIE;
        return null;
    }
}
