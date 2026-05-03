package com.cireonapp.server.controller;

import com.cireonapp.server.domain.config.Config;
import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.domain.user.UserPermissions;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import com.cireonapp.server.dto.ResponseDto;
import com.cireonapp.server.dto.UpdateConfigRequestDto;
import com.cireonapp.server.util.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static com.cireonapp.server.util.CookieHelper.getAuthCookie;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    @GetMapping("/get")
    public static ResponseEntity<?> get() {
        return ResponseEntity.ok(ConfigManager.get());
    }

    @PostMapping("/update")
    public static ResponseEntity<?> update(@Valid @RequestBody UpdateConfigRequestDto newConfig, HttpServletRequest request) {

        Optional<Cookie> authCookie = CookieHelper.getAuthCookie(request);
        if(authCookie.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        Optional<Session> session = SessionManager.get(authCookie.get().getValue());

        if(session.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        Optional<User> user = UserManager.get(session.get().getUsername());

        if(user.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponseDto.Error.NOT_LOGGED_IN);

        boolean isAdmin = user.get().getPermissions().contains(UserPermissions.ADMINISTRATOR);
        boolean canManageConfig = user.get().getPermissions().contains(UserPermissions.CONFIG_MANAGE);


        if(!isAdmin && !canManageConfig)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(CommonResponseDto.Error.INSUFFICIENT_PERMISSIONS);



        Config config = ConfigManager.get();
        if (newConfig.maxUsers != null) config.setMaxUsers(newConfig.maxUsers);
        if (newConfig.port != null) config.setPort(newConfig.port);

        boolean result = ConfigManager.update(config);
        if (result) return ResponseEntity.ok(new ResponseDto());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponseDto.Error.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String,String> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String,String> errors = new java.util.HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMessageNotReadableExceptions(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.JSON_PARSING_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedExceptions(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.INVALID_JSON_BODY);
    }
}
