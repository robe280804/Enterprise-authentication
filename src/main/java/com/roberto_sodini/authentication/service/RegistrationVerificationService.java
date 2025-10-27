package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.exceptions.TokenExpired;
import com.roberto_sodini.authentication.exceptions.TokenNotFound;
import com.roberto_sodini.authentication.model.RegistrationVerification;
import com.roberto_sodini.authentication.repository.RegistrationVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationVerificationService {

    private final RegistrationVerificationRepository registrationVerificationRepository;
    private final PasswordEncoder encoder;

    /**
     * <p> Il metodo esegue i seguenti passaggi </p>
     * <ul>
     *     <li> Creo un token a breve scadenza ed eseguo l'hash </li>
     *     <li> Eseguo l'hash della password dell'utente </li>
     *     <li> Imposto tutti i token dell'utente come revoked, per evitare conflitti </li>
     *     <li> Creo il model e lo salvo nel db, ritorno il token</li>
     * </ul>
     * @param userEmail dell utente
     * @param password dell'utente
     * @return token
     */
    @Transactional
    public String create(String userEmail, String password){
        log.info("[REGISTRATION_VERIFICATION_CREATE] Creazione token per {}", userEmail);

        String token = UUID.randomUUID().toString();
        String hashToken = DigestUtils.sha3_256Hex(token);
        String hashPassword = encoder.encode(password);

        registrationVerificationRepository.revokedAllByUserEmail(userEmail);

        RegistrationVerification registrationVerification = RegistrationVerification.builder()
                .userEmail(userEmail)
                .userPassword(hashPassword)
                .token(hashToken)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .registerSuccess(false)
                .revoked(false)
                .build();

        registrationVerificationRepository.save(registrationVerification);

        log.info("[REGISTRATION_VERIFICATION_CREATE] Token creato con successo");
        return token;
    }

    /**
     * <p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Eseguo l'hash del token con il solito metodo in cui l'ho creato </li>
     *     <li> Ottengo il model valido (con token non scaduto) dal db, se non esiste lancio un eccezione </li>
     *     <li> Imposto con una query il model con success=true e registerSuccess=true </li>
     *     <li> Ritorno il model</li>
     * </ul>
     * @param token a breve scadenza in chiaro
     * @return model con i dati dell'utente
     * @exception TokenNotFound se il token non esiste o è scaduto
     * @exception IllegalStateException se la query fallisce
     */
    @Transactional
    public RegistrationVerification verifyToken(String token){
        log.info("[REGISTRATION_VERIFICATION_VERIFY] Validazione del token");

        String hashToken = DigestUtils.sha3_256Hex(token);

        RegistrationVerification registrationVerification = registrationVerificationRepository.findValidToken(hashToken, LocalDateTime.now())
                .orElseThrow(() -> new TokenNotFound("Token non esistente o scaduto"));

        int queryResult = registrationVerificationRepository.revokedAndConfirmToken(registrationVerification.getId());

        if (queryResult != 1) {
            throw new IllegalStateException("Fallimento nell'aggiornare lo stato del token dopo la registrazione.");
        }

        log.info("[TOKEN_REGISTRATION_VERIFY] Token valido");
        return registrationVerification;
    }
}
