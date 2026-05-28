package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.domain.media.source.SourceType;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CommonMedia {
    private final SourceType sourceType;
    private final long created;
    private long lastUpdated;

    public CommonMedia(SourceType sourceType) {
        this.sourceType = sourceType;
        this.created = Timestamp.valueOf(LocalDateTime.now()).getTime();
        this.lastUpdated = Timestamp.valueOf(LocalDateTime.now()).getTime();

    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = Timestamp.valueOf(LocalDateTime.now()).getTime();
    }

    public long getCreated() {
        return created;
    }
}
