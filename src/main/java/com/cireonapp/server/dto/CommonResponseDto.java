package com.cireonapp.server.dto;

public class CommonResponseDto {
    public static class Error {
        public static final ErrorResponseDto INCORRECT_USERNAME_PASSWORD = new ErrorResponseDto("Incorrect username or password!");
        public static final ErrorResponseDto INVALID_JSON_BODY = new ErrorResponseDto("Body must be in JSON format!");
        public static final ErrorResponseDto JSON_PARSING_ERROR = new ErrorResponseDto("There was an error parsing the request body!");
        public static final ErrorResponseDto NOT_LOGGED_IN = new ErrorResponseDto("Not logged in! Please log in to perform this action.");
        public static final ErrorResponseDto INSUFFICIENT_PERMISSIONS = new ErrorResponseDto("You don't have the required permissions to perform this action!");
        public static final ErrorResponseDto USERNAME_ALREADY_EXISTS = new ErrorResponseDto("A user with this username already exists!");
        public static final ErrorResponseDto INTERNAL_SERVER_ERROR = new ErrorResponseDto("An error occurred while processing your request! Please check the console for more details.");
        public static final ErrorResponseDto MISSING_PARAMETERS = new ErrorResponseDto("Missing required parameters!");
    }
}
