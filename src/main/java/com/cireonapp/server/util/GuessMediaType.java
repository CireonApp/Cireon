package com.cireonapp.server.util;

import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.domain.media.source.SourceType;

public class GuessMediaType {
    public static SourceType guess(String hash) {
        if (MovieManager.get(hash).isPresent())
            return SourceType.MOVIE;
        return null;
    }
}
