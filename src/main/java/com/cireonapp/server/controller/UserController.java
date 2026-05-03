package com.cireonapp.server.controller;

import com.cireonapp.server.domain.config.ConfigManager;
import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.LoginResponseDto;
import com.cireonapp.server.dto.NewUserRequestDto;
import jakarta.validation.Valid;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserRequestDto user) {

        int maxUsers = ConfigManager.get().getMaxUsers();
        int userCount = UserManager.getAll().size();

        if(userCount >= maxUsers) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Maximum number of users reached. Max users: " + maxUsers);
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

}
