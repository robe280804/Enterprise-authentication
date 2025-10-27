package com.roberto_sodini.authentication.controller;

import com.roberto_sodini.authentication.dto.CreateUserDto;
import com.roberto_sodini.authentication.dto.UserDto;
import com.roberto_sodini.authentication.security.ratelimiter.RateLimit;
import com.roberto_sodini.authentication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RateLimit(timesWindowSecond = 60, limit = 3)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/")
    public ResponseEntity<UserDto> getUser(){
        return ResponseEntity.ok(userService.getUser());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUser());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid CreateUserDto request){
        return ResponseEntity.status(201).body(userService.createUser(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable UUID userId){
        return ResponseEntity.ok(userService.delete(userId));
    }

}
