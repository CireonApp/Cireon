package com.cireonapp.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Success Response",
        description = "Response DTO for success responses. Contains an success message and a timestamp."
)
public class SuccessResponseDto extends ResponseDto {
    @Schema(description = "Success message describing the successful operation that occurred")
    public String successMessage;

    public SuccessResponseDto(String successMessage) {
        this.successMessage = successMessage;
    }
}
