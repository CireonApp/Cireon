package com.cireonapp.server.controller;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.dto.ErrorResponseDto;
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
@RequestMapping("/user")
public class UserController {

    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody NewUserRequestDto user) {
        User newUser = new User(user.username,user.password,user.displayName);

        boolean success = UserManager.createUser(newUser);
        if(success)
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDto(newUser.getDisplayName(), newUser.getPermissions()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto("An error occurred while creating the user! Please check the console for more details."));
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
    @ExceptionHandler(UniqueConstraintException.class)
    public ResponseEntity<?> handleUniqueConstraintExceptions(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("A user with this Username already exists!"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMessageNotReadableExceptions(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("There was an error parsing the request body!"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedExceptions(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("Body must be in JSON format!"));
    }

}
