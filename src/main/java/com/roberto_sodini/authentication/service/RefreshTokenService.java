package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.model.RefreshToken;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.RefreshTokenRepository;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import com.roberto_sodini.authentication.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    @Transactional
    public String create(String email){
        log.info("[CREATE REFRESH TOKEN] Creazione refresh token per utente {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotRegister("Email non trovata"));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(false, userDetails);
        String hashToken = DigestUtils.sha3_256Hex(token);

        // Query per evitare N+1
        refreshTokenRepository.revokedAllForUserEmail(email);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .refreshToken(hashToken)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("[CREATE REFRESH TOKEN] Refresh token creato con successo");

        return refreshToken.getRefreshToken();
    }
}
