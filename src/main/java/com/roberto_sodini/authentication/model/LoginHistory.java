package com.roberto_sodini.authentication.model;

import com.roberto_sodini.authentication.enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
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
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Enumerated(EnumType.STRING)
    private AuthProvider loginProvider;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason")
    private String failureReason;
}
