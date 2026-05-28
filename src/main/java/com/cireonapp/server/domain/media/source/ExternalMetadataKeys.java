package com.cireonapp.server.domain.media.source;

import com.cireonapp.server.domain.media.common.ExternalMetadataSources;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        hidden = true,
        name = "External Metadata Keys",
        description = "Contains the external metadata keys for a source. This is used to store the external metadata keys for a source."
)
public class ExternalMetadataKeys {
    @Schema(description = "Preferred external metadata source for this source. This is used to determine which external metadata source to use when fetching metadata for media items in this source.")
    private ExternalMetadataSources preferredExternalSource;
    @Schema(description = "API key for TheMovieDB")
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
