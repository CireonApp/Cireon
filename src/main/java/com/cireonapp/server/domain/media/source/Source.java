package com.cireonapp.server.domain.media.source;

import org.dizitart.no2.repository.annotations.Id;

import java.nio.file.Path;
import java.util.UUID;

public class Source {
    @Id
    private String id;
    private Path dirPath;
    private SourceType type;
    private boolean enabled;
    private boolean watchForChanges;
    private String name;
    private String description;
    private ExternalMetadataKeys externalMetadataKeys;
    private String preferredLanguage;

    public Source(String id, Path dirPath, SourceType type, boolean enabled, boolean watchForChanges, String name, String description, ExternalMetadataKeys externalMetadataKeys, String preferredLanguage) {
        this.id = id;
        this.dirPath = dirPath;
        this.type = type;
        this.enabled = enabled;
        this.watchForChanges = watchForChanges;
        this.name = name;
        this.description = description;
        this.externalMetadataKeys = externalMetadataKeys;
        this.preferredLanguage = preferredLanguage;
    }

    public Source() {
        id = UUID.randomUUID().toString();
        enabled = true;
        watchForChanges = true;
    }

    public Path getDirPath() {
        return dirPath;
    }

    public void setDirPath(Path dirPath) {
        this.dirPath = dirPath;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * If enabled is set to false, watchForChanges will also be set to false. If enabled is set to true, watchForChanges will not be changed.
     *
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        if (!enabled)
            this.watchForChanges = false;
        this.enabled = enabled;
    }

    public boolean isWatchForChanges() {
        return watchForChanges;
    }

    /**
     * If watchForChanges is set to true, enabled will also be set to true. If watchForChanges is set to false, enabled will not be changed.
     *
     * @param watchForChanges
     */
    public void setWatchForChanges(boolean watchForChanges) {
        if (watchForChanges)
            this.enabled = true;
        this.watchForChanges = watchForChanges;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public ExternalMetadataKeys getExternalMetadataKeys() {
        return externalMetadataKeys;
    }

    public void setExternalMetadataKeys(ExternalMetadataKeys externalMetadataKeys) {
        this.externalMetadataKeys = externalMetadataKeys;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
