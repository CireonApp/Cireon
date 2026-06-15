package com.cireonapp.server.domain.config;

import com.cireonapp.server.initializer.Databases;

import com.cireonapp.server.service.FFmpegServices;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;

public class ConfigManager {

    /**
     * Fetches the current configuration from the database. If no configuration exists, returns a default Config instance.
     *
     * @return
     */
    public static Config get() {
        Config config = Databases.getConfigRepository().find().firstOrNull();
        return config != null ? config : new Config();
    }

    /**
     * Updates the configuration in the database. If no configuration exists, it will be created.
     *
     * @param newConfig The new configuration to save.
     * @return true if the configuration was updated successfully, false otherwise.
     */
    public static boolean update(Config newConfig) {
        Config config = get();
        config.merge(newConfig);
        if (newConfig.getEncoder() != null) {
            FFmpegServices.setEncoder(newConfig.getEncoder(), false);
        }
        WriteResult result = Databases.getConfigRepository().update(Filter.ALL, config);
        return result.getAffectedCount() > 0;
    }

    public static boolean reset() {
        Filter filter = config -> true; // Update all documents (there should only be one)
        WriteResult result = Databases.getConfigRepository().update(filter, new Config());
        return result.getAffectedCount() > 0;
    }
}
