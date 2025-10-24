package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final LoginHistoryService loginHistoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/logins")
    public ResponseEntity<List<LoginHistoryDto>> loginHistory(){
        return ResponseEntity.ok(loginHistoryService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/login")
    public ResponseEntity<List<LoginHistoryDto>> userLoginHistory(@RequestBody EmailDto request){
        return ResponseEntity.ok(loginHistoryService.getUserLogin(request));
    }
}
