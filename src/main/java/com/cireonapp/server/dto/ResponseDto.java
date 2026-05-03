package com.cireonapp.server.dto;

import com.cireonapp.server.util.TimeHelper;

public class ResponseDto {
    public String timestamp;

    public ResponseDto(){
        this.timestamp = TimeHelper.getCurrentTimeISO();
    }
}
