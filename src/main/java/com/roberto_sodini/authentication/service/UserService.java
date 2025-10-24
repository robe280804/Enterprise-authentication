package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.CreateUserDto;
import com.roberto_sodini.authentication.dto.UserDto;
import com.roberto_sodini.authentication.exceptions.EmailAlredyRegistered;
import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.mapper.UserMapper;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import com.roberto_sodini.authentication.security.audit.AuditAction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;

    @AuditAction(action = "GET_USER_INFO", logFinalResault = true)
    public UserDto getUser() {
        UUID userId = getUserId();
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EmailNotRegister("User non trovato"));
    }

    @AuditAction(action = "GET_ALL_USER_INFO")
    public List<UserDto> getAllUser() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @AuditAction(action = "GET_ALL_USER_INFO", logFinalResault = true)
    public Boolean delete(UUID userId) {
        if (!userRepository.existsById(userId)){
            return false;
        }
        userRepository.deleteById(userId);
        return true;
    }

    @AuditAction(action = "CREATE_USER", logFinalResault = true)
    public UserDto createUser(@Valid CreateUserDto request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlredyRegistered("Utente già registrato con email " + request.getEmail());
        }

        String hashPassword = encoder.encode(request.getPassword());
        User newUser = userMapper.toModel(request, hashPassword);

        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    private UUID getUserId(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails){
            return userDetails.getId();
        }
        throw new AuthenticationCredentialsNotFoundException("Non sei autorizzatto");
    }

}
