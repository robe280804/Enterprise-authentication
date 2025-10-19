package com.roberto_sodini.authentication.producer;

import com.roberto_sodini.authentication.dto.LoginHistoryDto;
import com.roberto_sodini.authentication.model.LoginHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginHistoryProducer {

    private final KafkaTemplate<String, LoginHistoryDto> kafkaTemplate;

    public void sendLoginHistory(LoginHistoryDto loginHistory){
        kafkaTemplate.send("login-history-events", loginHistory)
                        .whenComplete((res, ex) -> {
                            if (ex == null){
                                log.info("[LOGIN HISTORY PRODUCER] Evento login {} inviato con successo a {}",
                                        loginHistory, res.getRecordMetadata().topic());
                            } else {
                                log.error("[LOGIN HISTORY PRODUCER] Errore durante l'invio ", ex);
                            }
                        });
    }
}
