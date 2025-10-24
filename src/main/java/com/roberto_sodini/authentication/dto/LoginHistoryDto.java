package com.roberto_sodini.authentication.dto;

import com.roberto_sodini.authentication.enums.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {

    private String userEmail;


    private LocalDateTime loginTime;


    private AuthProvider loginProvider;


    private String ipAddress;


    private String userAgent;


    private boolean success;


    private String failureReason;
}
