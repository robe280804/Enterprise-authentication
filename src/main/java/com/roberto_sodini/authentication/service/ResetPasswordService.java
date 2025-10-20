package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.model.ResetPassword;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.RefreshTokenRepository;
import com.roberto_sodini.authentication.repository.ResetPasswordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordService {

    private final ResetPasswordRepository resetPasswordRepository;

    public String createToken(User user){
        log.info("[RESET PASSWORD SERVICE] Creazione del token per {}", user.getEmail());

        String token = UUID.randomUUID().toString();
        String hashToken = DigestUtils.sha3_256Hex(token);

        List<ResetPassword> userTokens = resetPasswordRepository.findAllByUser(user);

        if (!userTokens.isEmpty()){
            userTokens.forEach(tkn -> tkn.setRevoked(true));
            resetPasswordRepository.saveAll(userTokens);
        }

        ResetPassword resetPassword = ResetPassword.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .revoked(false)
                .build();

    }
}
