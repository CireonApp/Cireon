package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceManager;
import org.dizitart.no2.repository.Cursor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;


public class SourceSubscription implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        ServerApplication.LOGGER.info("Subscribing to source changes...");
        Cursor<Source> sources = SourceManager.getAll(true, true);
        for (Source source : sources) {
            FileWatcher.registerSource(source);
        }
    }
}
