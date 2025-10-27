package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.ResetPasswordDto;
import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.exceptions.PasswordInvalid;
import com.roberto_sodini.authentication.exceptions.TokenExpired;
import com.roberto_sodini.authentication.exceptions.TokenNotFound;
import com.roberto_sodini.authentication.model.ResetPassword;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.ResetPasswordRepository;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.audit.AuditAction;
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
    @AuditAction(action = "RESET_PASSWORD")
    @Transactional
    public String resetPassword(@Valid EmailDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EmailNotRegister("Utente non trovato"));

        String token = createToken(user);

        return sendResetPasswordEmail(token, request.getEmail());
    }

    /**
     *
     * @param request
     * @return
     */
    @Transactional
    public String saveNewPassword(@Valid ResetPasswordDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordInvalid("Le password devoo essere uguali");
        }
        ResetPassword resetPassword = validateToken(request.getToken());

        User user = resetPassword.getUser();

        String newPassword = encoder.encode(request.getPassword());
        user.setPassword(newPassword);
        userRepository.save(user);

        log.info("[RESET_PASSWORD] Password aggiornata con succesoo");
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
        // Link immaginario, ci dovrebbe essere la pagina frontend con il form del reset
        var.put("confirmLink", "https://localhost:8443/views/reset-password?token=" + token);

        emailService.sendHtmlEmail("reset_password", email, emailUsername, var);

        return "Ti è stata inviata un email per creare una nuova password, token=" + token;
    }
}
