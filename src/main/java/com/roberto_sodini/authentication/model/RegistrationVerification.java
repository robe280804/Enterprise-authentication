package com.roberto_sodini.authentication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "register_token")
public class RegistrationVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_password_hash", nullable = false)
    private String userPassword;

    @Column(nullable = false)
    private String token;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean revoked;

    @Column(name = "registerSuccess")
    private Boolean registerSuccess;
}
