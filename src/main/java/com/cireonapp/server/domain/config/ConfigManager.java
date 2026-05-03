package com.cireonapp.server.domain.config;

import com.cireonapp.server.initializer.Databases;

import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;

public class ConfigManager {

    /**
     * Fetches the current configuration from the database. If no configuration exists, returns a default Config instance.
     *
     * @return
     */
    public static Config get() {
        Config config = Databases.configRepository.find().firstOrNull();
        return config != null ? config : new Config();
    }

    /**
     * Updates the configuration in the database. If no configuration exists, it will be created.
     *
     * @param newConfig The new configuration to save.
     * @return true if the configuration was updated successfully, false otherwise.
     */
    public static boolean update(Config newConfig) {
        Filter filter = config -> true; // Update all documents (there should only be one)
        WriteResult result = Databases.configRepository.update(filter, newConfig);
        return result.getAffectedCount() > 0;
    }

    public static boolean reset() {
        Filter filter = config -> true; // Update all documents (there should only be one)
        WriteResult result = Databases.configRepository.update(filter, new Config());
        return result.getAffectedCount() > 0;
    }
}
