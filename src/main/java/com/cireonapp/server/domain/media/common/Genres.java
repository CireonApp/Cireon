package com.cireonapp.server.domain.media.common;

import info.movito.themoviedbapi.model.core.Genre;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import java.util.Optional;
import java.util.Set;

public enum Genres {

    SOAP("Soap"),
    TALK("Talk"),
    WAR_POLITICS("War & Politics"),
    SCIFI_FANTASY("Sci-Fi & Fantasy"),
    NEWS("News"),
    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FAMILY("Family"),
    FANTASY("Fantasy"),
    KIDS("Kids"),
    HISTORY("History"),
    HORROR("Horror"),
    MUSIC("Music"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    REALITY("Reality"),
    SCIENCE_FICTION("Science Fiction"),
    TV_MOVIE("TV Movie"),
    THRILLER("Thriller"),
    WAR("War"),
    WESTERN("Western");

    public final String label;

    Genres(String label) {
        this.label = label;
    }

    public static final String[] labels() {
        Genres[] genres = Genres.values();
        String[] genreStrings = new String[genres.length];
        for (int i = 0; i < genres.length; i++) {
            genreStrings[i] = genres[i].label;
        }
        return genreStrings;
    }


    public static Optional<Genres> searchGenre(String query) {
        ExtractedResult result = FuzzySearch.extractOne(query, Set.of(Genres.labels()));
        return stringToGenre(result.getString());
    }

    public static Optional<Genres> stringToGenre(String genre) {
        for (Genres g : Genres.values()) {
            if (g.label.equalsIgnoreCase(genre)) {
                return Optional.of(g);
            }
        }
        return Optional.empty(); // or throw an exception if you prefer
    }

    public static String[] GenresToStringArray(Set<Genres> genresSet) {
        String[] genres = new String[genresSet.size()];
        int i = 0;
        for (Genres genre : genresSet) {
            genres[i++] = genre.label;
        }
        return genres;
    }


}
