package com.cireonapp.server.dto;

import com.cireonapp.server.domain.media.source.ExternalMetadataKeys;
import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Source Response",
        description = "Response DTO for a source. Contains all the information about a source."
)
public class SourceResponseDto extends ResponseDto{
    @Schema(description = "ID of the source")
    public String id;
    @Schema(description = "The directory the source is scanning")
    public String dirPath;
    @Schema(description = "Type of source: Movie, Show, ect..")
    public SourceType type;
    @Schema(description = "Whether the source is enabled or not")
    public boolean enabled;
    @Schema(description = "Whether to watch the dirPath for any file changes")
    public boolean watchForChanges;
    @Schema(description = "The name of the source")
    public String name;
    @Schema(description = "Short description of the source")
    public String description;
    public ExternalMetadataKeys externalMetadataKeys;
    @Schema(description = "Preferred language for metadata. Should be a ISO 639-1 code, like 'en' for English or 'fr' for French")
    public String preferredLanguage;

    public SourceResponseDto(){
        super();
    }

    public static SourceResponseDto get(Source source) {
        SourceResponseDto dto = new SourceResponseDto();
        dto.id = source.getId();
        dto.dirPath = source.getDirPath().toString();
        dto.type = source.getType();
        dto.enabled = source.isEnabled();
        dto.watchForChanges = source.isWatchForChanges();
        dto.name = source.getName();
        dto.description = source.getDescription();
        dto.externalMetadataKeys = source.getExternalMetadataKeys();
        dto.preferredLanguage = source.getPreferredLanguage();
        return dto;
    }
}
