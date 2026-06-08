package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.util.TextSimilarityHelper;

import java.util.Locale;
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
        if (query == null || query.isBlank()) return Optional.empty();

        String normalizedQuery = query.toLowerCase(Locale.ROOT).trim();

        Genres bestMatch = null;
        int bestScore = -1;

        for (Genres genre : Genres.values()) {
            int score = TextSimilarityHelper.weightedRatio(
                    normalizedQuery,
                    genre.label.toLowerCase(Locale.ROOT)
            );
            if (score > bestScore) {
                bestScore = score;
                bestMatch = genre;
            }
        }

        // Tune this threshold (e.g. 60-80) depending on how strict you want matching
        return bestScore >= 70 ? Optional.of(bestMatch) : Optional.empty();
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
