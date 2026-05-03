package com.cireonapp.server.util.external;

import com.cireonapp.server.ServerApplication;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.Images;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;

public class MovieDB {
    public static MovieResultsPage searchMovie(String apiKey, String query, String language, String year) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(apiKey);
        if (year.trim().isEmpty()) year = null; // If the year is empty, set it to null to ignore it in the search
        return tmdbApi.getSearch().searchMovie(query, true, language, null, 1, null, year); // Return the search results
    }

    public static MovieDb getMovie(String apiKey, int movieId, String language) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(apiKey);
        return tmdbApi.getMovies().getDetails(movieId, language);
    }

    public static Images getArtwork(String apiKey, int movieId, String language) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(apiKey);
        return tmdbApi.getMovies().getImages(movieId, "en-US,null","en-US");

    }
}
