package com.cireonapp.server.controller.backend;

import com.cireonapp.server.dto.CommonResponseDto;
import com.cireonapp.server.dto.ErrorResponseDto;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.naming.NoPermissionException;
import java.io.FileNotFoundException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<String> handleRestClientTooManyRequests(HttpClientErrorException.TooManyRequests ignored) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too Many Requests, please try again in a few moments.");
    }

    // Catching Spring WebFlux/WebClient 429 exceptions (Since your POM includes WebFlux)
    @ExceptionHandler(WebClientResponseException.TooManyRequests.class)
    public ResponseEntity<String> handleWebClientTooManyRequests(WebClientResponseException.TooManyRequests ignored) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Too Many Requests, please try again in a few moments.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMessageNotReadableExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.JSON_PARSING_ERROR);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.INVALID_JSON_BODY);
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

    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<?> handleUniqueConstraintExceptions() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CommonResponseDto.Error.USERNAME_ALREADY_EXISTS);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleArrayIndexOutOfBoundsException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto("You are missing a request parameter!"));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto(ex.getMessage() != null ? ex.getMessage() : "The requested content was not found!"));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponseDto> handlePermissionException(NoPermissionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

}
