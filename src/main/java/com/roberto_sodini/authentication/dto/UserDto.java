package com.roberto_sodini.authentication.dto;

import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserDto {

    private UUID userId;
    private String email;
    private AuthProvider provider;
    private Set<Role> roles;
    private LocalDateTime createdAt;
}
