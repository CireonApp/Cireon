package com.cireonapp.server.dto;

import com.cireonapp.server.util.TimeHelper;

public class ErrorResponseDto extends ResponseDto {
    public String errorMessage;

    public ErrorResponseDto(String errorMessage) {
        this.errorMessage = errorMessage;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}
