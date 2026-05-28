package com.cireonapp.server.dto;

import com.cireonapp.server.util.TimeHelper;
import io.swagger.v3.oas.annotations.media.Schema;

public class ResponseDto {
    @Schema(description = "Timestamp of the response in ISO-8601 format")
    private final String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public ResponseDto(){
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}
