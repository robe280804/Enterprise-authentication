package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.EmailDto;
import com.roberto_sodini.authentication.dto.ResetPasswordRequestDto;
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
     *<p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Ottengo l'utente dall'email, se non esiste lancio un eccezione </li>
     *     <li> Invio un email al client con il token </li>
     * </ul>
     * @param request DTO contenente l'email
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
     *<p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Controllo che la password e la confirmPassword siano ugali </li>
     *     <li> Valido il token e, se positivo ottengo l'utente </li>
     *     <li> Eseguo l'hash della password e aggiorno l'utente </li>
     * </ul>
     * @param request DTO con token, password e confirmPassword
     * @return messaggio di successo
     */
    @Transactional
    public String saveNewPassword(@Valid ResetPasswordRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordInvalid("Le password devoo essere uguali");
        }
        ResetPassword resetPassword = validateToken(request.getToken());

        User user = resetPassword.getUser();

        String newPassword = encoder.encode(request.getPassword());
        int queryResult = userRepository.setNewPassword(newPassword, user.getId());

        if (queryResult != 1) {
            throw new IllegalStateException("Fallimento nell'aggiornare la password dell'utente");
        }

        log.info("[RESET_PASSWORD] Password aggiornata con succesoo");
        return "Password cambiata con successo";
    }


    @Transactional
    private String createToken(User user){
        log.info("[RESET PASSWORD SERVICE] Creazione del token per {}", user.getEmail());

        // Creo un token e ne eseguo l'hash
        String token = UUID.randomUUID().toString();
        String hashToken = DigestUtils.sha3_256Hex(token);

        // Query per evitare N+1
        // Imposto come revoked tutti i token precedenti per evitare conflitti
        resetPasswordRepository.revokedAllByUser(user);

        ResetPassword resetPassword = ResetPassword.builder()
                .user(user)
                .token(hashToken)
                .expiryDate(LocalDateTime.now().plusMinutes(5))  //scadenza breve
                .revoked(false)
                .build();

        resetPasswordRepository.save(resetPassword);

        log.info("[RESET PASSWORD SERVICE] Token creato con successo");
        return resetPassword.getToken();
    }

    @Transactional
    private ResetPassword validateToken(String token){
        log.info("[RESET PASSWORD VALIDATE] Validazione del token in esecuzione");

        // Ottengo attraverso una query personalizzata un model con revoked=false e che non sia expire
        ResetPassword resetPassword = resetPasswordRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new TokenNotFound("Token non trovato"));

        // Imposto il token come revoked
        int queryResult = resetPasswordRepository.revokedAndConfirmToken(resetPassword.getId());

        if (queryResult != 1) {
            throw new IllegalStateException("Fallimento nell'aggiornare lo stato del token");
        }

        log.info("[RESET PASSWORD VALIDATE] Token valido");
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
