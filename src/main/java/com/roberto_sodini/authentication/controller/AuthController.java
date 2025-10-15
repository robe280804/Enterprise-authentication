package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.AccessRequestDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid AccessRequestDto request){
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid AccessRequestDto request){
        return ResponseEntity.ok(authService.login(request));
    }
}
