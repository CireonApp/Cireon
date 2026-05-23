package com.cireonapp.server.domain.media.source;

import com.cireonapp.server.domain.media.common.ExternalMetadataSources;

public class ExternalMetadataKeys {
    private ExternalMetadataSources preferredExternalSource;
    private String TMDB;

    public ExternalMetadataKeys() {
    }

    public String getTMDB() {
        return TMDB;
    }

    public void setTMDB(String TMDB) {
        this.TMDB = TMDB;
    }

    public ExternalMetadataSources getPreferredExternalSource() {
        return preferredExternalSource;
    }

    public void setPreferredExternalSource(ExternalMetadataSources preferredExternalSource) {
        this.preferredExternalSource = preferredExternalSource;
    }
}
