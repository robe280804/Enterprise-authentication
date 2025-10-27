package com.roberto_sodini.authentication.controller;


import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.ResetPasswordRequestDto;
import com.roberto_sodini.authentication.security.ratelimiter.RateLimit;
import com.roberto_sodini.authentication.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @RateLimit(timesWindowSecond = 60, limit = 3)
    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid EmailDto request){
        return ResponseEntity.ok(resetPasswordService.resetPassword(request));
    }

    @RateLimit(limit = 3, timesWindowSecond = 60)
    @PostMapping("/save")
    public ResponseEntity<String> saveNewPassword(@RequestBody @Valid ResetPasswordRequestDto reqeust){
        return ResponseEntity.ok(resetPasswordService.saveNewPassword(reqeust));
    }
}
