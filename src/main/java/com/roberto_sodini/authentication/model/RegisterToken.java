package com.roberto_sodini.authentication.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "register_token")
public class RegisterToken {

    private Long id;
    private UUID userId;
    private String token;
    private LocalDateTime expiryDate;
    private Boolean revoked;
}
