package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.model.LoginHistory;
import com.roberto_sodini.authentication.repository.LoginHistoryRepository;
import com.roberto_sodini.authentication.security.audit.AuditAction;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final AuthMapper authMapper;


    public LoginHistoryDto create(HttpServletRequest servletRequest, String userEmail, AuthProvider provider){
        String userIp = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-agent");

        return LoginHistoryDto.builder()
                .ipAddress(userIp)
                .userEmail(userEmail)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .loginProvider(AuthProvider.LOCALE)
                .build();
    }

    public void save(LoginHistoryDto loginHistoryDto){
        LoginHistory loginHistory = authMapper.loginHistory(loginHistoryDto);
        loginHistoryRepository.save(loginHistory);
    }

    @AuditAction(action = "GET_LOGIN_HISTORY")
    public List<LoginHistoryDto> getAll() {
        return loginHistoryRepository.findAll().stream()
                .map(authMapper::loginHistoryDto)
                .toList();
    }

    @AuditAction(action = "GET_USER_LOGIN_HISTORY")
    public List<LoginHistoryDto> getUserLogin(EmailDto request) {
        return  loginHistoryRepository.findAllByUserEmail(request.getEmail()).stream()
                .map(authMapper::loginHistoryDto)
                .toList();
    }
}
