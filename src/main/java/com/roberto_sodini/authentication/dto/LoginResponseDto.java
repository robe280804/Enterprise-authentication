package com.roberto_sodini.authentication.dto;

import com.roberto_sodini.authentication.enums.AuthProvider;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponseDto {

    private String message;
    private UUID userId;
    private String email;
    private AuthProvider provider;
    private String accessToken;
}
