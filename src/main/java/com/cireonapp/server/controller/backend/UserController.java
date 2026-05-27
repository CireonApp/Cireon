package com.cireonapp.server.controller.backend;

import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.dto.*;
import com.cireonapp.server.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Tag(name = "User API", description = "User related endpoints")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Operation(
            summary = "Create a user.",
            description = "Create a new user."
    )
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserRequestDto user, HttpServletRequest request) {


        int maxUsers = ConfigManager.get().getMaxUsers();
        long userCount = UserManager.getAll().size();

        Optional<User> authenticatedUser = CookieHelper.getUserFromSessionCookie(request);

        boolean isAdmin = authenticatedUser.isPresent() && authenticatedUser.get().getPermissions().contains(UserPermissions.ADMINISTRATOR);
        boolean canManageUsers = authenticatedUser.isPresent() && authenticatedUser.get().getPermissions().contains(UserPermissions.USER_MANAGE);


        if (!isAdmin && !canManageUsers)
            if (userCount >= maxUsers) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("Maximum number of users reached."));
            }

        User newUser = new User(user.username, user.password, user.displayName);

        boolean success = UserManager.create(newUser);
        if (success)
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDto(newUser.getDisplayName(), newUser.getPermissions()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new java.util.HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<?> handleUniqueConstraintExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.USERNAME_ALREADY_EXISTS);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMessageNotReadableExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.JSON_PARSING_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.INVALID_JSON_BODY);
    }

    @Operation(
            summary = "Delete a user.",
            description = "Delete a user. You  can delete your own user when authenticated or if you have the right permissions, you can delete any user. Leave username param to delete your username."
    )
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam(value = "username", required = false) String username, HttpServletRequest request) {

        Optional<Cookie> cookie = CookieHelper.getAuthCookie(request);
        if (cookie.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        Optional<Session> session = SessionManager.get(cookie.get().getValue());
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }

        Optional<User> user = UserManager.get(session.get().getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommonResponseDto.Error.NOT_LOGGED_IN);
        }


        if (username == null || username.equals(user.get().getUsername())) {
            boolean deleteReq = UserManager.delete(user.get());
            if (deleteReq)
                return ResponseEntity.ok(new SuccessResponseDto("User deleted successfully"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
        }

        Optional<User> userFromParam = UserManager.get(username);
        if (userFromParam.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CommonResponseDto.Error.USER_NOT_FOUND);
        }

        Set<UserPermissions> permissions = user.get().getPermissions();
        if (permissions.contains(UserPermissions.ADMINISTRATOR) ||
                permissions.contains(UserPermissions.USER_MANAGE)
        ) {
            boolean deleteReq = UserManager.delete(username);
            if (deleteReq)
                return ResponseEntity.ok(new SuccessResponseDto("User deleted successfully"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
        }


        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);

    }
}
