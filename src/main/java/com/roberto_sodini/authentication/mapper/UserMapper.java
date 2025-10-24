package com.roberto_sodini.authentication.mapper;

import com.roberto_sodini.authentication.dto.CreateUserDto;
import com.roberto_sodini.authentication.dto.UserDto;
import com.roberto_sodini.authentication.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user){
        return UserDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toModel(CreateUserDto userDto, String hashPassword){
        return User.builder()
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .roles(userDto.getRole())
                .provider(userDto.getProvider())
                .build();
    }
}
