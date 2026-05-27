package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.common.ParsedName;
import com.cireonapp.server.domain.media.common.SearchResults;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.ContentHashHelper;
import com.cireonapp.server.util.InternetConnection;
import info.movito.themoviedbapi.tools.TmdbException;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.io.FilenameUtils;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.repository.Cursor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;

import static com.cireonapp.server.util.external.MovieDB.*;

public class MovieManager {

    private static final DateTimeFormatter RELEASE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    public static List<SearchResults<Movie>> search(String query, int limit) {
        if (query == null || query.isBlank() || limit <= 0) return List.of();

        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        PriorityQueue<SearchResults<Movie>> topMatches = new PriorityQueue<>(
                limit,
                Comparator.comparingInt(SearchResults::score)
        );

        for (Movie movie : getAll()) {
            String title = movie.getMetadata() != null ? movie.getMetadata().getTitle() : null;
            if (title == null || title.isBlank()) continue;

            int score = FuzzySearch.weightedRatio(normalizedQuery, title.toLowerCase(Locale.ROOT));
            if (score <= 70) continue;

            SearchResults<Movie> result = new SearchResults<>(movie, score);
            if (topMatches.size() < limit) {
                topMatches.offer(result);
            } else if (score > topMatches.peek().score()) {
                topMatches.poll();
                topMatches.offer(result);
            }
        }

        List<SearchResults<Movie>> results = new ArrayList<>(topMatches);
        results.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return results;
    }

    public static List<Movie> getByCreationDate(SortOrder order, int limit) {
        return new ArrayList<>(Databases.movieRepository.find(Filter.ALL, FindOptions.orderBy("created", order).limit(limit)).toList());
    }

    public static List<Movie> getByReleaseDate(SortOrder order, int limit) {
        return new ArrayList<>(Databases.movieRepository.find(Filter.ALL, FindOptions.orderBy("metadata.releaseDateTimestamp", order).limit(limit)).toList());
    }

    public static Optional<Movie> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(Databases.movieRepository.getById(id));
    }

    public static Cursor<Movie> getAll() {
        return Databases.movieRepository.find(Filter.ALL);
    }

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "mpeg", "mpg", "webm"
    );


    public static void handleMovieSourceUpdate(Source source) {
        try (Stream<Path> stream = Files.walk(source.getDirPath())) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(MovieManager::isSupported)
                    .forEach(file -> processMovie(file, source));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isSupported(Path file) {
        String ext = FilenameUtils.getExtension(file.toString()).toLowerCase();
        return SUPPORTED_FORMATS.contains(ext);
    }

    private static void processMovie(Path file, Source source) {
        try {
            if (!InternetConnection.isConnected())
                return;

            String hash = ContentHashHelper.hashFile(file);
            Movie existing = Databases.movieRepository.getById(hash);

            if (existing != null) {
                updateExisting(existing, file);
                return;
            }

            ParsedName parsed = ParsedName.parseName(file);
            if (parsed.getName() == null || parsed.getName().isBlank()) {
                ServerApplication.LOGGER.warn("Could not parse a usable name from file, skipping: " + file.getFileName());
                return;
            }
            MovieMetadata metadata = fetchMetadata(parsed, source);

            if (metadata == null) return;

            Movie movie = new Movie();
            movie.setHash(hash);
            movie.setFilePath(file);
            movie.setMetadata(metadata);

            Databases.movieRepository.insert(movie);

        } catch (Exception e) {
            ServerApplication.LOGGER.error(e.getMessage());
        }
    }

    private static void updateExisting(Movie movie, Path newPath) {
        if (!movie.getFilePath().equals(newPath)) {
            movie.setFilePath(newPath);
            Databases.movieRepository.update(movie);
        }

        LocalDateTime lastUpdated = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(movie.getMetadata().getLastUpdated()),
                ZoneId.systemDefault()
        );
        if (LocalDateTime.now().isBefore(
                lastUpdated.plusDays(30))) {
            ServerApplication.LOGGER.info("Metadata for " + movie.getMetadata().getTitle() + " is up to date, skipping update.");
            return;
        }

        Databases.movieRepository.remove(movie);
    }

    private static MovieMetadata fetchMetadata(ParsedName parsed, Source source) throws TmdbException {
        switch (source.getExternalMetadataKeys().getPreferredExternalSource()) {
            case TMDB:
                return fetchMetadata_tmdb(parsed, source);
        }
        return fetchMetadata_tmdb(parsed, source);
    }
}
