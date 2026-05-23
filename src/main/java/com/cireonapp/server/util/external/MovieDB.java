package com.cireonapp.server.util.external;

import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.common.Genres;
import com.cireonapp.server.domain.media.common.ParsedName;
import com.cireonapp.server.domain.media.movie.MovieMetadata;
import com.cireonapp.server.domain.media.source.Source;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.Images;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.appendtoresponse.MovieAppendToResponse;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.cireonapp.server.domain.media.common.Artwork.saveImage;

public class MovieDB {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

    public static MovieResultsPage searchMovie(String apiKey, String query, String language, String year) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(apiKey);
//        if (year) year = null; // If the year is empty, set it to null to ignore it in the search
        return tmdbApi.getSearch().searchMovie(query, true, language, null, 1, null, year); // Return the search results
    }

    public static MovieDb getMovie(String apiKey, int movieId, String language) throws TmdbException {
        TmdbApi tmdbApi = new TmdbApi(apiKey);
        return tmdbApi.getMovies().getDetails(movieId, language, MovieAppendToResponse.IMAGES,MovieAppendToResponse.ALTERNATIVE_TITLES);
    }

//    public static Images getArtwork(String apiKey, int movieId, String language) throws TmdbException {
//        TmdbApi tmdbApi = new TmdbApi(apiKey);
//        return tmdbApi.getMovies().getImages(movieId, " ", language, "en-US", "null");
//
//    }

    public static String saveImageSafe(String path, int id, String type) {
        try {
            String ext = path.substring(path.lastIndexOf('.') + 1);
            return saveImage(IMAGE_BASE_URL + path, String.valueOf(id), type, ext);
        } catch (Exception e) {
            return null;
        }
    }

    public static MovieMetadata fetchMetadata_tmdb(ParsedName name, Source source) throws TmdbException {
        MovieResultsPage search = searchMovie(
                source.getExternalMetadataKeys().getTMDB(),
                name.getName(),
                Objects.requireNonNullElse(source.getPreferredLanguage(), "en-US"),
                name.getYear()
        );

        if (search.getResults().isEmpty()) return null;

        var result = search.getResults().getFirst();

        MovieMetadata metadata = new MovieMetadata();
        metadata.setId(result.getId());
        metadata.setTitle(result.getTitle());
        metadata.setOriginalTitle(result.getOriginalTitle());
        metadata.setDescription(result.getOverview());
        metadata.setAdult(result.getAdult());
        metadata.setReleaseDate(parseDate(result.getReleaseDate()));
        metadata.setReleaseDateTimestamp(convertToTimestamp(metadata.getReleaseDate()));


        MovieDb details = attachDetails_tmdb(metadata, result.getId(), source);


        attachImages_tmdb(metadata, result.getId(), details.getImages());


        metadata.setLastUpdated(Timestamp.valueOf(LocalDateTime.now()).getTime());
        return metadata;
    }

    private static void attachImages_tmdb(MovieMetadata metadata, int movieId, Images images) throws TmdbException {
        Artwork art = new Artwork();

        if (!images.getPosters().isEmpty()) {
            art.setPoster(saveImageSafe(images.getPosters().getFirst().getFilePath(), movieId, "poster"));
        }

        if (!images.getLogos().isEmpty()) {
            art.setLogo(saveImageSafe(images.getLogos().getFirst().getFilePath(), movieId, "logo"));
        }

        if (!images.getBackdrops().isEmpty()) {
            art.setBackground(saveImageSafe(images.getBackdrops().getFirst().getFilePath(), movieId, "background"));
        }

        metadata.setArtworks(art);
    }

    private static MovieDb attachDetails_tmdb(MovieMetadata metadata, int movieId, Source source) throws TmdbException {
        String language = null;

        if (source.getPreferredLanguage() == null || source.getPreferredLanguage().equals("")) {
            language = "null,en-US";
        } else {
            language = String.format("null,%s", source.getPreferredLanguage());
        }
        MovieDb details = getMovie(
                source.getExternalMetadataKeys().getTMDB(),
                movieId,
                language
        );

        metadata.setTagline(details.getTagline());
        metadata.setRuntime(details.getRuntime());

        details.getGenres().forEach(g ->
                Genres.searchGenre(g.getName())
                        .ifPresent(metadata.getGenres()::add)
        );

        if (details.getAlternativeTitles() != null) {
            details.getAlternativeTitles()
                    .getTitles()
                    .forEach(t -> metadata.getAlternativeTitles().add(t.getTitle()));
        }

        return details;
    }


    private static String parseDate(String date) {
        if (date == null || date.isEmpty()) return null;
        String[] splitted = date.split("-");
        return String.format("%s/%s/%s", splitted[1], splitted[2], splitted[0]);
    }

    public static long convertToTimestamp(String dateStr) {
        // 1. Define the input pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        // 2. Parse the string into a LocalDate object
        LocalDate date = LocalDate.parse(dateStr, formatter);

        // 3. Convert to timestamp at the start of that day (using System Default TimeZone)
        return date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
