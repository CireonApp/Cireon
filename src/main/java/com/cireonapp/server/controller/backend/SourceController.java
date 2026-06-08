package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.dto.SourceResponseDto;
import com.cireonapp.server.dto.SuccessResponseDto;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.dizitart.no2.repository.Cursor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Tag(name = "Sources API", description = "Sources related endpoints")
@RestController
@RequestMapping("/api/source")
public class SourceController {

    @Operation(summary = "Get all sources.",
            description = "Get all sources. Can filter by enabled and watchForChanges flags. Must be an administrator or have content manage permissions to use this endpoint.")
    @GetMapping("/all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all sources",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = SourceResponseDto.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User is not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> getAllSources(@RequestParam(value = "onlyEnabled", defaultValue = "false")
                                           boolean onlyEnabled,
                                           @RequestParam(value = "onlyWatchForChanges", defaultValue = "false")
                                           boolean onlyWatchForChanges, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);

        if (!user.get().isAdministrator())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);


        Cursor<Source> sources = SourceManager.getAll(onlyEnabled, onlyWatchForChanges);

        return ResponseEntity.ok(sources.toSet());
    }

    @Operation(summary = "Get a source.",
            description = "Get a sources by an id. Must be an administrator or have content manage permissions to use this endpoint.")
    @GetMapping("/get")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved a sources by given ID.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SourceResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User is not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Source with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )
    })
    public ResponseEntity<?> getSource(@RequestParam(value = "id")
                                       String id,
                                       HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);

        if (!user.get().isAdministrator())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);


        Optional<Source> source = SourceManager.get(id);

        if (source.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDto("Source not found"));

        return ResponseEntity.ok(source.get());
    }

    @Operation(
            summary = "Delete a source.",
            description = "Delete a source. Only admins can manage sources."
    )
    @DeleteMapping("/delete")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted a source.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = SuccessResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User is not logged in",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have sufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - A source with the given id was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - An error occurred while processing your request.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - You are missing a parameter.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ErrorResponseDto.class
                            )
                    )
            )
    }
    )
    public ResponseEntity<?> delete(@RequestParam(value = "id") String sourceID, HttpServletRequest request) {

        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);


        if(!user.get().isAdministrator()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);
        }

        Optional<Source> sourceFromParam = SourceManager.get(sourceID);
        if (sourceFromParam.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponseDto.Error.CONTENT_NOT_FOUND);
        }

        boolean deleteReq = SourceManager.delete(sourceFromParam.get());

        if(deleteReq){
            return ResponseEntity.ok(new SuccessResponseDto("Source was deleted successfully!"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
    }

}
