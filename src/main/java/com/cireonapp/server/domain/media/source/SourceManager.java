package com.cireonapp.server.domain.media.source;

import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.initializer.FileWatcher;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.Cursor;

import java.util.Optional;
import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;

public class SourceManager {
    public static Cursor<Source> getAll(boolean onlyEnabled, boolean onlyWatchForChanges) {
        Filter enabledFilter = where("enabled").eq(onlyEnabled);
        Filter watchForChangesFilter = where("watchForChanges").eq(onlyWatchForChanges);

        if (onlyEnabled && onlyWatchForChanges) {
            return Databases.sourceRepository.find(Filter.and(enabledFilter, watchForChangesFilter));
        } else if (onlyEnabled) {
            return Databases.sourceRepository.find(enabledFilter);
        } else if (onlyWatchForChanges) {
            return Databases.sourceRepository.find(watchForChangesFilter);
        }

        return Databases.sourceRepository.find(Filter.ALL);
    }

    public static boolean create(Source source) {
        source.setId(UUID.randomUUID().toString()); //Force a random ID
        WriteResult result = Databases.sourceRepository.insert(source);

        if (source.isWatchForChanges()) {
            FileWatcher.registerSource(source);
        }

        return result.getAffectedCount() > 0;
    }

    public static Optional<Source> get(String id) {
        Source source = Databases.sourceRepository.getById(id);
        return Optional.ofNullable(source);
    }
}
