package com.cireonapp.server.dto;

import com.cireonapp.server.util.TimeHelper;

public class SuccessResponseDto extends ResponseDto {
    public String successMessage;

    public SuccessResponseDto(String successMessage) {
        this.successMessage = successMessage;
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}
