package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.common.MediaInfoByFFmpeg;
import com.cireonapp.server.domain.media.common.ParsedName;
import com.cireonapp.server.domain.media.common.SearchResults;
import com.cireonapp.server.domain.media.common.VideoMediaFile;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.service.FFmpegServices;
import com.cireonapp.server.util.ContentHashHelper;
import com.cireonapp.server.util.InternetConnection;
import com.cireonapp.server.util.TextSimilarityHelper;
import info.movito.themoviedbapi.tools.TmdbException;
import org.apache.commons.io.FilenameUtils;
import org.dizitart.no2.collection.FindOptions;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.repository.Cursor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

import static com.cireonapp.server.domain.media.common.CommonMedia.SUPPORTED_FORMATS;
import static com.cireonapp.server.util.external.MovieDB.fetchMetadata_tmdb;
import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.filters.FluentFilter.where;

public class MovieManager {

    //
//    //  possibly add an option for threshold in the future for now hardcoded to 60 which is pretty balanced
    public static List<SearchResults<Movie>> search(String query, int limit, boolean mustHaveFiles) {
        if (query == null || query.isBlank() || limit <= 0) return List.of();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        PriorityQueue<SearchResults<Movie>> topMatches = new PriorityQueue<>(limit, Comparator.comparingInt(SearchResults::score));

        for (Movie movie : getAll(mustHaveFiles)) {
            String title = movie.getMetadata() != null ? movie.getMetadata().getTitle() : null;
            if (title == null || title.isBlank()) continue;

            int score = TextSimilarityHelper.weightedRatio(normalizedQuery, title.toLowerCase(Locale.ROOT));
            if (score <= 60) continue;

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

    public static List<Movie> getByCreationDate(SortOrder order, int limit, boolean mustHaveFiles) {
        if (mustHaveFiles) {
            return Databases.getMovieRepository().find(
                    where("hasFiles").eq(true),
                    FindOptions.orderBy("created", order).limit(limit)
            ).toList();
        }

        return Databases.getMovieRepository().find(
                ALL,
                FindOptions.orderBy("created", order).limit(limit)
        ).toList();
    }

    public static List<Movie> getByReleaseDate(SortOrder order, int limit, boolean mustHaveFiles) {
        if (mustHaveFiles) {
            return Databases.getMovieRepository().find(
                    where("hasFiles").eq(true),
                    FindOptions.orderBy("metadata.releaseDateTimestamp", order).limit(limit)
            ).toList();
        }

        return Databases.getMovieRepository().find(
                ALL,
                FindOptions.orderBy("metadata.releaseDateTimestamp", order).limit(limit)
        ).toList();
    }

    public static Movie get(String movieId) {
        return Databases.getMovieRepository().getById(movieId);
    }

    /**
     * will return a cursor of movies that contain a file with the given hash.
     * If no movies are found will return an empty cursor.
     *
     * @param hash
     * @return
     */
    public static Cursor<Movie> getMovieByHash(String hash) {
        Cursor<Movie> movies = Databases.getMovieRepository().find(where("files").elemMatch(where("hash").eq(hash)));

        return movies;

    }

    public static VideoMediaFile getFileByHash(String hash) {
        Cursor<Movie> movies = Databases.getMovieRepository().find(where("files").elemMatch(where("hash").eq(hash)));
        if (movies.isEmpty()) return null;

        Movie movie = movies.firstOrNull();
        if (movie == null || movie.getFiles().isEmpty()) return null;

        return movie.getFileByHash(hash);
    }

    public static Cursor<Movie> getAll(boolean mustHaveFiles) {
        if (mustHaveFiles) {
            // FIX: Use the global static elemMatch filter syntax
            return Databases.getMovieRepository().find(
                    where("hasFiles").eq(true));
        }
        return Databases.getMovieRepository().find(ALL);
    }


    public static boolean isSupported(Path file) {
        String ext = FilenameUtils.getExtension(file.toString()).toLowerCase();
        return SUPPORTED_FORMATS.contains(ext);
    }

    public static void handleMovieSourceUpdate(Source source) {
        try (Stream<Path> stream = Files.walk(source.getDirPath())) {
            stream.filter(Files::isRegularFile).filter(MovieManager::isSupported).forEach(file -> processMovie(file, source));
        } catch (IOException ignored) {
        }
    }

    public static void handleMovieFolderUpdate(Path folder,Source source) {
        try (Stream<Path> stream = Files.walk(folder)) {
            stream.filter(Files::isRegularFile).filter(MovieManager::isSupported).forEach(file -> processMovie(file, source));
        } catch (IOException ignored) {
        }
    }

    public static void deleteAllMoviesInsideDir(Path dir) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile).forEach(MovieManager::deleteMovieFile);
        } catch (IOException ignored) {
        }
    }


    public static void processMovie(Path file, Source source) {
        try {
            if (!isSupported(file)) return;
            MediaInfoByFFmpeg FFmpegInfo = FFmpegServices.getVideoInfo(file.toString());
            if (FFmpegInfo == null) return;

            if (!InternetConnection.isConnected()) {
                return;
            }

            String contentHash = ContentHashHelper.hashFile(file);


            Cursor<Movie> existingMovies = getMovieByHash(contentHash);

            boolean foundExisting = false;
            for (Movie existingMovie : existingMovies) {
                updateExisting(existingMovie, file, FFmpegInfo, source, contentHash);
                foundExisting = true;
                deleteAllUnsupportedFileTypes(existingMovie);
            }
            if (foundExisting) return;

            // New file - parse metadata to find or create movie
            ParsedName parsed = ParsedName.parseName(file);
            if (parsed.getName() == null || parsed.getName().isBlank()) {
                return;
            }

            MovieMetadata metadata = fetchMetadata(parsed, source);
            if (metadata == null) {
                return;
            }

            // Find movie by metadata, ID is probably universal across all external services since we are using IMDB ID.
            Movie movieByMetadata = findMovieByMetadataId(metadata.getId());

            if (movieByMetadata != null) {
                VideoMediaFile videoFile = createVideoMediaFile(file, FFmpegInfo, contentHash);
                movieByMetadata.addFile(videoFile);
                movieByMetadata.setHasFiles(true);
                Databases.getMovieRepository().update(movieByMetadata);
            } else {
                Movie movie = new Movie();
                movie.setMetadata(metadata);
                VideoMediaFile videoFile = createVideoMediaFile(file, FFmpegInfo, contentHash);
                movie.setFiles(new ArrayList<>());
                movie.addFile(videoFile);
                movie.setHasFiles(true);

                Databases.getMovieRepository().insert(movie);
            }



        } catch (Exception e) {
            ServerApplication.LOGGER.error("Error processing movie file " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    public static void deleteAllUnsupportedFileTypes(Movie movie) {
        for (VideoMediaFile file : movie.getFiles()) {
            if (!isSupported(file.getFilePath())) {
                movie.removeFile(file.getHash());
            }
        }
        Databases.getMovieRepository().update(movie, false);
    }

    private static VideoMediaFile createVideoMediaFile(Path file, MediaInfoByFFmpeg ffmpegInfo, String contentHash) {
        VideoMediaFile videoFile = new VideoMediaFile();

        // Use content-based hash instead of path hash
        videoFile.setHash(contentHash);
        videoFile.setFilePath(file);
        videoFile.setDuration(ffmpegInfo.getDuration());
        videoFile.setFps(ffmpegInfo.getFps());
        videoFile.setAudioCodec(ffmpegInfo.getAudioCodec());
        videoFile.setVideoCodec(ffmpegInfo.getVideoCodec());
        videoFile.setWidth(ffmpegInfo.getWidth());
        videoFile.setHeight(ffmpegInfo.getHeight());
        return videoFile;
    }

    private static Movie findMovieByMetadataId(String metadataId) {
        Cursor<Movie> movies = Databases.getMovieRepository().find(where("metadata.id").eq(metadataId));
        return movies.firstOrNull();
    }

    /**
     * Removes movies that have had no files for more than the specified number of days.
     * Call this periodically to clean up orphaned movies from renames/deletes.
     *
     * @param daysThreshold Only delete movies empty for this many days (e.g., 7)
     * @return Number of movies deleted
     */
    public static int cleanupEmptyMovies(int daysThreshold) {
        int deletedCount = 0;
        long thresholdMillis = System.currentTimeMillis() - (daysThreshold * 24L * 60 * 60 * 1000);

        Cursor<Movie> allMovies = getAll(false); // Get all movies, even empty ones

        for (Movie movie : allMovies) {
            if (movie.getFiles() == null || movie.getFiles().isEmpty()) {
                // Check if movie has been empty long enough
                if (movie.getLastUpdated() < thresholdMillis) {
                    Databases.getMovieRepository().remove(movie);
                    deletedCount++;
                }
            }
        }

        return deletedCount;
    }

    private static void updateExisting(Movie movie, Path newPath, MediaInfoByFFmpeg ffmpegInfo, Source source, String contentHash) {

        // 1. Find or create VideoMediaFile for this content hash
        VideoMediaFile existingFile = movie.getFileByHash(contentHash);

        if (existingFile != null) {
            // Update existing file - path may have changed due to rename/move
            VideoMediaFile updates = createVideoMediaFile(newPath, ffmpegInfo, contentHash);
            existingFile.merge(updates);
        } else {
            // New file for this movie
            VideoMediaFile newFile = createVideoMediaFile(newPath, ffmpegInfo, contentHash);
            movie.addFile(newFile);
        }

        // 2. Check if metadata is older than 30 days
        LocalDateTime lastUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(movie.getMetadata().getLastUpdated()), ZoneId.systemDefault());

        if (LocalDateTime.now().isAfter(lastUpdated.plusDays(30))) {
            try {
                // Re-parse and fetch fresh metadata
                ParsedName parsed = ParsedName.parseName(newPath);
                MovieMetadata freshMetadata = fetchMetadata_tmdb(parsed, source);
                if (freshMetadata != null) {
                    movie.setMetadata(freshMetadata);
                }
            } catch (Exception e) {
                ServerApplication.LOGGER.error("Failed to refresh metadata for updated path: " + e.getMessage());
            }
        }

        // 3. Save changes only once if anything actually changed
        movie.setHasFiles(!movie.getFiles().isEmpty());
        Databases.getMovieRepository().update(movie);
    }

    private static MovieMetadata fetchMetadata(ParsedName parsed, Source source) throws TmdbException {
        switch (source.getExternalMetadataKeys().getPreferredExternalSource()) {
            case TMDB:
                return fetchMetadata_tmdb(parsed, source);
            default:
                return fetchMetadata_tmdb(parsed, source);
        }
    }

    public static void deleteMovieFile(Path filePath) {
        try {
            Path normalizedPath = filePath.normalize();

            // Find all movies that have files matching this path (exact or inside this directory)
            // cant search with hash since we use this method for files that dont exist anymore.
            Cursor<Movie> allMovies = getAll(true);
            List<Movie> moviesToUpdate = new ArrayList<>();

            for (Movie movie : allMovies) {
                if (movie.getFiles() == null) continue;

                boolean hasMatchingFiles = false;
                for (VideoMediaFile file : movie.getFiles()) {
                    if (file.getFilePath() != null) {
                        Path filePath_normalized = file.getFilePath().normalize();
                        // Check if this file matches the deleted path (exact or inside directory)
                        if (filePath_normalized.equals(normalizedPath) || filePath_normalized.startsWith(normalizedPath)) {
                            hasMatchingFiles = true;
                            break;
                        }
                    }
                }

                if (hasMatchingFiles) {
                    moviesToUpdate.add(movie);
                }
            }

            if (moviesToUpdate.isEmpty()) return;


            // Process each affected movie
            for (Movie movie : moviesToUpdate) {
                // Remove all files that match the deleted path
                List<VideoMediaFile> filesToRemove = new ArrayList<>();
                for (VideoMediaFile file : movie.getFiles()) {
                    if (file.getFilePath() != null) {
                        Path filePath_normalized = file.getFilePath().normalize();
                        if (filePath_normalized.equals(normalizedPath) || filePath_normalized.startsWith(normalizedPath)) {
                            filesToRemove.add(file);
                        }
                    }
                }

                // Remove the files
                for (VideoMediaFile fileToRemove : filesToRemove) {
                    movie.removeFile(fileToRemove.getHash());
                }

                // update and not delete. preserves the movie UUID
                // Empty movies may be temporarily empty due to renames/moves
                // we dont delete a movie automatically because there are no files.
                // allow admins to cleanup empty movies manually by sources.
                movie.setHasFiles(!movie.getFiles().isEmpty());
                Databases.getMovieRepository().update(movie);
            }

        } catch (Exception ignored) {
        }
    }
}
