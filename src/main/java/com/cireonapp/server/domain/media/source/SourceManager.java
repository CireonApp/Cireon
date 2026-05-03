package com.cireonapp.server.domain.media.source;

import com.cireonapp.server.initializer.Databases;
import org.dizitart.no2.filters.Filter;

import java.util.Set;

import static org.dizitart.no2.filters.FluentFilter.where;

public class SourceManager {
    public static Set<Source> getAll(boolean onlyEnabled, boolean onlyWatchForChanges) {
        Filter enabledFilter = where("enabled").eq(onlyEnabled);
        Filter watchForChangesFilter = where("watchForChanges").eq(onlyWatchForChanges);

        if (onlyEnabled && onlyWatchForChanges) {
            return Databases.sourceRepository.find(Filter.and(enabledFilter, watchForChangesFilter)).toSet();
        } else if (onlyEnabled) {
            return Databases.sourceRepository.find(enabledFilter).toSet();
        } else if (onlyWatchForChanges) {
            return Databases.sourceRepository.find(watchForChangesFilter).toSet();
        }

        return Databases.sourceRepository.find(Filter.ALL).toSet();
    }
}
