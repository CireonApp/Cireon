package com.cireonapp.server.domain.media.movie;

import com.cireonapp.server.domain.media.common.Artwork;
import com.cireonapp.server.domain.media.common.ParsedName;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.initializer.AppPath;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.ContentHashHelper;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.movies.Images;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.tools.TmdbException;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.io.FilenameUtils;
import org.dizitart.no2.filters.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cireonapp.server.util.external.MovieDB.*;

public class MovieManager {

    private record MovieSearchResult(Movie movie, int score) {
    }

    public static Set<Movie> search(String query) {
        return getAll().stream()
                .map(movie -> new MovieSearchResult(
                        movie,
                        FuzzySearch.weightedRatio(query.toLowerCase(), movie.getMetadata().getTitle().toLowerCase())
                ))
                .filter(result -> result.score() > 70)
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .map(MovieSearchResult::movie)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Optional<Movie> get(String id){
        if(id == null) return Optional.empty();
        return Optional.ofNullable(Databases.movieRepository.getById(id));
    }

    public static Set<Movie> getAll() {
        return Databases.movieRepository.find(Filter.ALL).toSet();
    }

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "mpeg", "mpg", "webm"
    );

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original";

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
            String hash = ContentHashHelper.hashFile(file);
            Movie existing = Databases.movieRepository.getById(hash);

            if (existing != null) {
                updateExisting(existing, file);
                return;
            }

            ParsedName parsed = parseName(file);
            MovieMetadata metadata = fetchMetadata(parsed, source);

            if (metadata == null) return;

            Movie movie = new Movie();
            movie.setHash(hash);
            movie.setFilePath(file);
            movie.setMetadata(metadata);

            Databases.movieRepository.insert(movie);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateExisting(Movie movie, Path newPath) {
        if (!movie.getFilePath().equals(newPath)) {
            movie.setFilePath(newPath);
            Databases.movieRepository.update(movie);
        }

        if (LocalDateTime.now().isAfter(
                movie.getMetadata().getLastUpdated().plusDays(30))) {

            Databases.movieRepository.remove(movie);
        }
    }

    private static MovieMetadata fetchMetadata(ParsedName name, Source source) throws TmdbException {
        MovieResultsPage search = searchMovie(
                source.getExternalMetadataKeys().getMovieDB(),
                name.getName(),
                source.getPreferredLanguage(),
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
        metadata.setReleaseDate(result.getReleaseDate());


        attachImages(metadata, result.getId(), source);
        attachDetails(metadata, result.getId(), source);


        metadata.setLastUpdated(LocalDateTime.now());
        return metadata;
    }

    private static void attachImages(MovieMetadata metadata, int movieId, Source source) throws TmdbException {
        Images images = getArtwork(
                source.getExternalMetadataKeys().getMovieDB(),
                movieId,
                source.getPreferredLanguage()
        );

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

    private static void attachDetails(MovieMetadata metadata, int movieId, Source source) throws TmdbException {
        MovieDb details = getMovie(
                source.getExternalMetadataKeys().getMovieDB(),
                movieId,
                source.getPreferredLanguage()
        );

        metadata.setTagline(details.getTagline());
        metadata.setRuntime(details.getRuntime());

        details.getGenres().forEach(g ->
                MovieGenres.stringToGenre(g.getName())
                        .ifPresent(metadata.getGenres()::add)
        );

        if (details.getAlternativeTitles() != null) {
            details.getAlternativeTitles()
                    .getTitles()
                    .forEach(t -> metadata.getAlternativeTitles().add(t.getTitle()));
        }
    }

    private static String saveImageSafe(String path, int id, String type) {
        try {
            String ext = path.substring(path.lastIndexOf('.') + 1);
            return saveImage(IMAGE_BASE_URL + path, String.valueOf(id), type, ext);
        } catch (Exception e) {
            return null;
        }
    }

    private static ParsedName parseName(Path filePath) {
        String file = filePath.getFileName().toString();
        file = file.replaceFirst("\\.[^.]+$", "");
        file = file.replaceAll("(?i)\\b(1337x|thepiratebay|tpb|torrentgalaxy|tgx|limetorrents|magnetdl|yts|yify|nyaa|fitgirl|repacks|eztv|rutracker|zooqle|kickasstorrents|kat|torrentz2|btdig|snowfl|animetosho|myanonamouse|mam|academictorrents|tamilrockers|extto|torlock|torrentdownloads|yourbittorrent|rarbg|rutor|ibit|demonoid|pirateiro|kinozal|btmet|qbittorrent|deluge|transmission|utorrent|biglybt|tixati|vuze)\\b", "");
        file = file.replaceAll("(?i)\\b(1080p|720p|2160p|4k|bluray|brrip|webrip|x264|x265|h264|h265)\\b", "");
        file = file.replaceAll("\\[.*?]", "");
        file = file.replaceAll("\\.", " ");
        file = file.trim();

        ParsedName result = new ParsedName();

        var match = file.matches(".*\\(\\d{4}\\)$");

        if (match) {
            int idx = file.lastIndexOf('(');
            result.setName(file.substring(0, idx).trim());
            result.setYear(file.substring(idx + 1, idx + 5));
            return result;
        }

        var yearMatcher = java.util.regex.Pattern.compile("(\\d{4})").matcher(file);
        if (yearMatcher.find()) {
            result.setYear(yearMatcher.group(1));
            file = file.replace(yearMatcher.group(1), "");
        }

        result.setName(file.trim());
        return result;
    }

    private static String saveImage(String url, String hash, String type, String ext) {
        try (InputStream in = new URL(url).openStream()) {
            Path out = AppPath.APP_DIR.resolve("data/content/" + hash + "_" + type + "." + ext);
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            return out.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
