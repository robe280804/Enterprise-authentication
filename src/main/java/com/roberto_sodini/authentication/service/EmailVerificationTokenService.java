package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.exceptions.TokenExpired;
import com.roberto_sodini.authentication.exceptions.TokenNotFound;
import com.roberto_sodini.authentication.model.EmailVerificationToken;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder encoder;

    /**
     * <p> Il metodo esegue i seguenti passaggi </p>
     * <ul>
     *     <li> Creo un token a breve scadenza </li>
     *     <li> Eseguo l'hash della password dell'utente </li>
     *     <li> Ottengo tutti i token che aveva l'utente e li imposto come revoked=true, per non avere conflitti con più token disponibili </li>
     *     <li> Creo il model e lo salvo nel db, ritorno il token</li>
     * </ul>
     * @param userEmail dell utente
     * @param password dell'utente
     * @return token
     */
    public String createToken(String userEmail, String password){
        log.info("[EMAIL VERIFICATION TOKEN] Creazione del token per l'utente {}", userEmail);

        String token = UUID.randomUUID().toString();
        String hashPassword = encoder.encode(password);

        List<EmailVerificationToken> userTokens = emailVerificationTokenRepository.findAll();

        if (!userTokens.isEmpty()){
            userTokens.forEach(tkn -> {
                tkn.setRevoked(true);
                emailVerificationTokenRepository.save(tkn);
            });

        }

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .userEmail(userEmail)
                .userPassword(hashPassword)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .revoked(false)
                .build();

        emailVerificationTokenRepository.save(emailVerificationToken);
        log.info("[EMAIL VERIFICATION TOKEN] Token creato con successo");

        return token;
    }


    public EmailVerificationToken verifyToken(String token){
        log.info("[EMAIL VERIFICATION TOKEN] Validazione del token");

        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFound("Token non esistente"));

        if (emailVerificationToken.getRevoked() || emailVerificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            log.warn("[EMAIL VARIFICATION TOKEN] Token scaduto per {}", emailVerificationToken.getUserEmail());
            throw new TokenExpired("Il tuo token è scaduto");
        }

        emailVerificationToken.setRevoked(true);
        emailVerificationToken.setRegisterSuccess(true);
        emailVerificationTokenRepository.save(emailVerificationToken);

        return emailVerificationToken;
    }
}
