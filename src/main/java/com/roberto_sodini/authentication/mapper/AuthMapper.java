package com.roberto_sodini.authentication.mapper;

import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.model.LoginHistory;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public RegisterResponseDto registerResponseDto(User user){
        return RegisterResponseDto.builder()
                .message("Registrazione avvenuta con successo")
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    public LoginResponseDto loginResponseDto(UserDetailsImpl userDetails, String accToken){
        return LoginResponseDto.builder()
                .message("Login eseguito con successo")
                .userId(userDetails.getId())
                .email(userDetails.getEmail())
                .provider(userDetails.getProvider())
                .accessToken(accToken)
                .build();
    }

    public LoginHistory loginHistory(LoginHistoryDto dto) {
        return LoginHistory.builder()
                .userEmail(dto.getUserEmail())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .loginProvider(AuthProvider.LOCALE)
                .success(dto.isSuccess())
                .failureReason(dto.getFailureReason())
                .loginTime(dto.getLoginTime())
                .build();
    }

    public LoginHistoryDto loginHistoryDto(LoginHistory loginHistory) {
        return LoginHistoryDto.builder()
                .userEmail(loginHistory.getUserEmail())
                .ipAddress(loginHistory.getIpAddress())
                .userAgent(loginHistory.getUserAgent())
                .loginProvider(AuthProvider.LOCALE)
                .success(loginHistory.isSuccess())
                .failureReason(loginHistory.getFailureReason())
                .loginTime(loginHistory.getLoginTime())
                .build();
    }
}
