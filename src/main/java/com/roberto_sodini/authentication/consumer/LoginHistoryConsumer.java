package com.roberto_sodini.authentication.consumer;

import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.model.LoginHistory;
import com.roberto_sodini.authentication.repository.LoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginHistoryConsumer {

    private final LoginHistoryRepository loginHistoryRepository;
    private final AuthMapper authMapper;

    @KafkaListener(topics = "login-history-events", groupId = "login-group")
    public void loginHistoryConsumer(LoginHistoryDto loginHistoryDto){
        try {
            LoginHistory loginHistory = authMapper.loginHistory(loginHistoryDto);
            loginHistoryRepository.save(loginHistory);
            log.info("[LOGIN HISTORY CONSUMER] Attività ricevuta e salvata con successo");

        } catch (Exception e){
            log.error("[LOGIN HISTORY CONSUMER] Errore elaborando l'evento {}", loginHistoryDto, e);
        }

    }
}
