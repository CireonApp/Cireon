package com.cireonapp.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;


public class UpdateConfigRequestDto {
    @Min(value = 0,message = "port must be above 0!")
    @Max(value = 65535,message = "port must be above 65535!")
    public Integer port;

    @Min(value = 0,message = "maxUsers must be above 0!")
    public Integer maxUsers;
}
