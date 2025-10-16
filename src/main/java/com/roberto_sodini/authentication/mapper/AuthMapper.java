package com.roberto_sodini.authentication.mapper;

import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
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
}
