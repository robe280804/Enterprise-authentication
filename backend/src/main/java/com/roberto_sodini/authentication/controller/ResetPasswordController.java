package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.ResetPasswordDto;
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

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid EmailDto request){
        return ResponseEntity.ok(resetPasswordService.resetPassword(request));
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<String> saveNewPassword(@RequestBody @Valid ResetPasswordDto reqeust){
        return ResponseEntity.ok(resetPasswordService.saveNewPassword(reqeust));
    }
}
