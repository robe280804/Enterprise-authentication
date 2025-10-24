package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.exceptions.TokenExpired;
import com.roberto_sodini.authentication.exceptions.TokenNotFound;
import com.roberto_sodini.authentication.model.EmailVerificationToken;
import com.roberto_sodini.authentication.repository.EmailVerificationTokenRepository;
import com.roberto_sodini.authentication.security.audit.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     *     <li> Creo un token a breve scadenza e ed eseguo l'hash </li>
     *     <li> Eseguo l'hash della password dell'utente </li>
     *     <li> Ottengo tutti i token che aveva l'utente e li imposto come revoked=true, per non avere conflitti con più token disponibili </li>
     *     <li> Creo il model e lo salvo nel db, ritorno il token</li>
     * </ul>
     * @param userEmail dell utente
     * @param password dell'utente
     * @return token
     */
    @Transactional
    public String createToken(String userEmail, String password){
        log.info("[TOKEN_REGISTRATION_CREATE] Creazione token per {}", userEmail);

        String token = UUID.randomUUID().toString();
        String hashToken = DigestUtils.sha3_256Hex(token);
        String hashPassword = encoder.encode(password);

        // Query per evitare N+1
        emailVerificationTokenRepository.revokedAllByUserEmail(userEmail);

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .userEmail(userEmail)
                .userPassword(hashPassword)
                .token(hashToken)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .registerSuccess(false)
                .revoked(false)
                .build();

        emailVerificationTokenRepository.save(emailVerificationToken);

        log.info("[TOKEN_REGISTRATION] Token creato con successo");
        return token;
    }

    /**
     * <p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Eseguo l'hash del token con il solito metodo in cui l'ho creato </li>
     *     <li> Ottengo il model dal db, se non esiste lancio un eccezione </li>
     *     <li> Controllo che il token presente nel model non sia scaduto, altrimenti lancio un eccezione </li>
     *     <li> Se è valido lo imposto come scaduto e lo salvo nel db, con register success = true </li>
     *     <li> Ritorno il model EmailVerificationToken che contiene i dati dell'utente per completare la registrazione </li>
     * </ul>
     * @param token a breve scadenza in chiaro
     * @return model con i dati dell'utente
     * @exception TokenNotFound se il token non esiste
     * @exception TokenExpired se è scaduto
     */
    @Transactional
    public EmailVerificationToken verifyToken(String token){
        log.info("[TOKEN_REGISTRATION_VERIFY] Validazione del token");

        String hashToken = DigestUtils.sha3_256Hex(token);

        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByToken(hashToken)
                .orElseThrow(() -> new TokenNotFound("Token non esistente"));

        if (emailVerificationToken.getRevoked() || emailVerificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            log.warn("[EMAIL VARIFICATION TOKEN] Token scaduto per {}", emailVerificationToken.getUserEmail());
            throw new TokenExpired("Il tuo token è scaduto");
        }

        emailVerificationToken.setRevoked(true);
        emailVerificationToken.setRegisterSuccess(true);
        emailVerificationTokenRepository.save(emailVerificationToken);

        log.info("[TOKEN_REGISTRATION_VERIFY] Token valido");
        return emailVerificationToken;
    }
}
