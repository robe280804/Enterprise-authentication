package com.roberto_sodini.authentication.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterResponseDto {

    private String message;
    private UUID userId;
    private String email;

}
