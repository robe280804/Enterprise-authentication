package com.roberto_sodini.authentication.mapper;

import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.model.User;

public class AuthMapper {

    public RegisterResponseDto registerResponseDto(User user){
        return RegisterResponseDto.builder()
                .message("Registrazione avvenuta con successo")
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }
}
