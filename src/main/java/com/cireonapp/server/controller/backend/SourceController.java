package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.media.source.Source;
import com.cireonapp.server.domain.media.source.SourceManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.dizitart.no2.repository.Cursor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@Tag(name = "Sources API", description = "Sources related endpoints")
@RestController
@RequestMapping("/api/source")
public class SourceController {

    @Operation(summary = "Get all sources.",
            description = "Get all sources. Can filter by enabled and watchForChanges flags. Must be an administrator or have content manage permissions to use this endpoint.")
    @GetMapping("/all")
    public ResponseEntity<?> getAllSources(@RequestParam(value = "onlyEnabled", defaultValue = "false")
                                           boolean onlyEnabled,
                                           @RequestParam(value = "onlyWatchForChanges", defaultValue = "false")
                                           boolean onlyWatchForChanges, HttpServletRequest request) {
        Optional<User> user = CookieHelper.getUserFromSessionCookie(request);
        if (user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);

        boolean isAdmin = user.get().getPermissions().contains(UserPermissions.ADMINISTRATOR);
        boolean canManageContent = user.get().getPermissions().contains(UserPermissions.CONTENT_MANAGE);

        if (!isAdmin && !canManageContent)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);

        Cursor<Source> sources = SourceManager.getAll(onlyEnabled, onlyWatchForChanges);

        return ResponseEntity.ok(sources.toSet());
    }
}
