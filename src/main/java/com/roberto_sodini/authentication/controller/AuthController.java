package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.AccessRequestDto;
import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.security.audit.AuditAction;
import com.roberto_sodini.authentication.security.ratelimiter.RateLimit;
import com.roberto_sodini.authentication.service.AuthService;
import com.roberto_sodini.authentication.service.ResetPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    @RateLimit(limit = 5, timesWindowSecond = 60)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid AccessRequestDto request){
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/confirm-register")
    public ResponseEntity<RegisterResponseDto> confirmRegister(@RequestParam String token){
        return ResponseEntity.status(201).body(authService.confirmRegister(token));
    }

    @RateLimit(limit = 5, timesWindowSecond = 60)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid AccessRequestDto request, HttpServletRequest servletRequest){
        return ResponseEntity.ok(authService.login(request, servletRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return ResponseEntity.ok(authService.logout());
    }
}
