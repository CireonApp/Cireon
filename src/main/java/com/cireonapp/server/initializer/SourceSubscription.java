package com.cireonapp.server.initializer;

import com.cireonapp.server.ServerApplication;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

public class SourceSubscription implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ServerApplication.LOGGER.info("Subscribing to source changes...");
        Set<Source> sources = SourceManager.getAll(true, true);
        for (Source source : sources) {
            FileWatcher.registerSource(source);
        }
    }
}
