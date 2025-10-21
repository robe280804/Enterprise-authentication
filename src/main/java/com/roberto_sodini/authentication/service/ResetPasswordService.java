package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.ResetPasswordDto;
import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.exceptions.TokenExpired;
import com.roberto_sodini.authentication.exceptions.TokenNotFound;
import com.roberto_sodini.authentication.model.ResetPassword;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.ResetPasswordRepository;
import com.roberto_sodini.authentication.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetPasswordService {

    @Value("${spring.mail.username}")
    private String emailUsername;

    private final ResetPasswordRepository resetPasswordRepository;
    private final UserRepository userRepository;
    private  final EmailService emailService;
    private final PasswordEncoder encoder;

    /**
     *
     * @param request
     * @return
     */
    public String resetPassword(@Valid EmailDto request) {
        log.info("[RESET PASSWORD REQUEST] Richiesta di reset password da {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EmailNotRegister("Utente non trovato"));

        String token = createToken(user);

        return sendResetPasswordEmail(token, request.getEmail());
    }

    /**
     *
     * @param reqeust
     * @return
     */
    public String saveNewPassword(@Valid ResetPasswordDto reqeust) {
        ResetPassword resetPassword = validateToken(reqeust.getToken());

        User user = resetPassword.getUser();

        String newPassword = encoder.encode(reqeust.getPassword());
        user.setPassword(newPassword);
        userRepository.save(user);

        return "Password cambiata con successo";
    }


    @Transactional
    private String createToken(User user){
        log.info("[RESET PASSWORD SERVICE] Creazione del token per {}", user.getEmail());

        String token = UUID.randomUUID().toString();
        String hashToken = DigestUtils.sha3_256Hex(token);

        // Query per evitare N+1
        resetPasswordRepository.revokedAllByUser(user);

        ResetPassword resetPassword = ResetPassword.builder()
                .user(user)
                .token(hashToken)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .revoked(false)
                .build();

        resetPasswordRepository.save(resetPassword);

        log.info("[RESET PASSWORD SERVICE] Token creato con successo");
        return resetPassword.getToken();
    }

    @Transactional
    private ResetPassword validateToken(String token){
        ResetPassword resetPassword = resetPasswordRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFound("Token non trovato"));

        if (resetPassword.getRevoked() || resetPassword.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new TokenExpired("Token scaduto o già utilizzato");
        }

        resetPassword.setRevoked(true);
        resetPasswordRepository.save(resetPassword);

        return resetPassword;
    }

    private String sendResetPasswordEmail(String token, String email){
        Map<String, Object> var = new HashMap<>();
        var.put("confirmLink", "http://localhost:8080/api/password/confirm?token=" + token);

        emailService.sendHtmlEmail("reset_password", email, emailUsername, var);

        log.info("[RESET PASSWORD REQUEST] Email inviata per il reset");
        return "Ti è stata inviata un email per creare una nuova password";
    }
}
