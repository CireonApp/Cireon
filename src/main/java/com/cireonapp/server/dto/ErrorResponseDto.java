package com.cireonapp.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(
        name = "Error Response",
        description = "Response DTO for error responses. Contains an error message and a timestamp."
)
public class ErrorResponseDto extends ResponseDto {
    @Schema(description = "Error message describing the error that occurred")
    public String errorMessage;

    public ErrorResponseDto(String errorMessage) {
        this.errorMessage = errorMessage;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}
