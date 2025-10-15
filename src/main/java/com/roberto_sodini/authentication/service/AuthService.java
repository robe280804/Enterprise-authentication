package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.AccessRequestDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import com.roberto_sodini.authentication.exceptions.EmailAlredyRegistered;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthMapper authMapper;

    public RegisterResponseDto register(@Valid AccessRequestDto request) {
        log.info("[REGISTER] Registrazione in esecuzione per {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("[REGISTER] Registrazione fallita, email {} già presente nel sistema", request.getEmail());
            throw new EmailAlredyRegistered("Email già registrata");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .provider(AuthProvider.LOCALE)
                .build();

        User savedUser = userRepository.save(newUser);

        log.info("[REGISTER] Registrazione avvenuta con successo per {}", savedUser.getEmail());
        return authMapper.registerResponseDto(savedUser);
    }

    public LoginResponseDto login(@Valid AccessRequestDto request) {
        log.info("[LOGIN] Login in esecuzione per {}", request.getEmail());

        if (!userRepository.existsByEmail(request.getEmail())){
            log.warn("[LOGIN] Login fallito, email {} non presente nel sistema", request.getEmail());
            throw new EmailNotRegistered("Email non registrata");
        }
    }
}
