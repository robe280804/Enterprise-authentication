package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.repository.LoginHistoryRepository;
import com.roberto_sodini.authentication.security.audit.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final AuthMapper authMapper;

    @AuditAction(action = "GET_LOGIN_HISTORY")
    public List<LoginHistoryDto> getAll() {
        return loginHistoryRepository.findAll().stream()
                .map(authMapper::toDto)
                .toList();
    }

    @AuditAction(action = "GET_USER_LOGIN_HISTORY")
    public List<LoginHistoryDto> getUserLogin(EmailDto request) {
        return  loginHistoryRepository.findAllByUserEmail(request.getEmail()).stream()
                .map(authMapper::toDto)
                .toList();
    }
}
