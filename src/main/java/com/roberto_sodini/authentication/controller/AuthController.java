package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.AuthRequestDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.security.ratelimiter.RateLimit;
import com.roberto_sodini.authentication.service.AuthService;
import com.roberto_sodini.authentication.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @RateLimit(limit = 3, timesWindowSecond = 60)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid AuthRequestDto request){
        return ResponseEntity.ok(authService.register(request));
    }

    @RateLimit(limit = 3, timesWindowSecond = 60)
    @GetMapping("/confirm-register")
    public ResponseEntity<RegisterResponseDto> confirmRegister(@RequestParam("token")String token){
        return ResponseEntity.status(201).body(authService.confirmRegister(token));
    }

    @RateLimit(limit = 3, timesWindowSecond = 60)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid AuthRequestDto request, HttpServletRequest servletRequest){
        return ResponseEntity.ok(authService.login(request, servletRequest));
    }

    @RateLimit(limit = 10, timesWindowSecond = 60)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return ResponseEntity.ok(authService.logout());
    }
}
