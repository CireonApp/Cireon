package com.cireonapp.server.domain.media.source;

import com.cireonapp.server.domain.media.movie.MovieManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.Cursor;

import java.util.Optional;
import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;

public class SourceManager {
    public static Cursor<Source> getAll() {
        return getAll(false, false);
    }

    public static Cursor<Source> getAll(boolean onlyEnabled, boolean onlyWatchForChanges) {
        Filter enabledFilter = where("enabled").eq(onlyEnabled);
        Filter watchForChangesFilter = where("watchForChanges").eq(onlyWatchForChanges);

        if (onlyEnabled && onlyWatchForChanges) {
            return Databases.getSourceRepository().find(Filter.and(enabledFilter, watchForChangesFilter));
        } else if (onlyEnabled) {
            return Databases.getSourceRepository().find(enabledFilter);
        } else if (onlyWatchForChanges) {
            return Databases.getSourceRepository().find(watchForChangesFilter);
        }

        return Databases.getSourceRepository().find(Filter.ALL);
    }

    public static boolean create(Source source) {
        source.setId(UUID.randomUUID().toString()); //Force a random ID
        WriteResult result = Databases.getSourceRepository().insert(source);

        if (source.isEnabled()) {
            if (source.getType() == SourceType.MOVIE)
                MovieManager.handleMovieSourceUpdate(source);
        }

        if (source.isWatchForChanges()) {
            FileWatcher.registerSource(source);
        }

        return result.getAffectedCount() > 0;
    }

    public static Optional<Source> get(String id) {
        Source source = Databases.getSourceRepository().getById(id);
        return Optional.ofNullable(source);
    }

    /**
     * Deletes a source from the database.
     *
     * @param source The source ID of the source to delete.
     * @return Returns true if the source was deleted successfully, false otherwise.
     */
    public static boolean delete(Source source) {
        WriteResult result = Databases.getSourceRepository().remove(source);
        FileWatcher.unregisterSource(source.getId());
        return result.getAffectedCount() > 0;
    }

    /**
     * Deletes a source from the database.
     *
     * @param sourceID The source ID of the source to delete.
     * @return Returns true if the source was deleted successfully, false otherwise.
     */
    public static boolean delete(String sourceID) {
        Optional<Source> source = get(sourceID);
        if (source.isEmpty()) return false;
        return delete(source.get());
    }
}
