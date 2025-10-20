package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.*;
import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import com.roberto_sodini.authentication.exceptions.EmailAlredyRegistered;
import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.exceptions.WrongAuthProvider;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.model.EmailVerificationToken;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.producer.LoginHistoryProducer;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import com.roberto_sodini.authentication.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    @Value("${jwt.expiration.refresh_token}")
    private Long longExpiration;

    @Value("${spring.mail.username}")
    private String emailUsername;

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final LoginHistoryProducer loginHistoryProducer;


    /**
     * <p> Il metodo esegue i seguenti passaggi: </p>
     * <li>
     *     <ul> Controllo che l'email non esista nel sistema </ul>
     *     <ul> Invio un email + token all'utente per confermare la registazione </ul>
     * </li>
     * @param request DTO con i dati dell'utente
     * @return messaggio per confermare l'invio dell'email
     * @exception EmailAlredyRegistered se l'email è gia registrata
     */
    public String register(@Valid AccessRequestDto request) {
        log.info("[REGISTER] Registrazione in esecuzione per {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())){
            log.warn("[REGISTER] Registrazione fallita, email {} già presente nel sistema", request.getEmail());
            throw new EmailAlredyRegistered("Email già registrata");
        }
        String token = verificationTokenService.createToken(request.getEmail(), request.getPassword());

        Map<String, Object> var = new HashMap<>();
        var.put("confirmLink", "http://localhost:8080/api/auth/confirm-register?token=" + token);

        emailService.sendHtmlEmail("register_confirm", request.getEmail(), emailUsername, var);

        return "Ti è stata inviata un email per confermare la registrazione";
    }

    public RegisterResponseDto confirmRegister(String token) {
        EmailVerificationToken emailVerificationToken = verificationTokenService.verifyToken(token);

        User newUser = User.builder()
                .email(emailVerificationToken.getUserEmail())
                .password(emailVerificationToken.getUserPassword())
                .roles(Set.of(Role.USER))
                .provider(AuthProvider.LOCALE)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("[REGISTER] Registrazione eseguita con successo per {}", newUser.getEmail());

        return authMapper.registerResponseDto(savedUser);
    }


    /**
     * <p> Il metodo esegue i seguenti step: </p>
     * <ul>
     *     <li> Ottengo l'indirizzo ip e l' agent user del client </li>
     *     <li> Creo un DTO login history che terrà traccia dei tentativi di login </li>
     *     <li> Controllo che l'utente sia registrato, se non lo è imposto il login history come non eseguito e lo invio a kafka </li>
     *     <li> Autentico l'utente attraverso authentication manager </li>
     *     <li> Controllo che il provider di accesso sia locale, altrimenti imposto il login history come non eseguito e lo invio a kafka </li>
     *     <li> Genero access token e refresh token, il quale salverò nel db (solo il refresh) </li>
     *     <li> Imposto il login history come eseguito e lo invio a kafka </li>
     *     <li> Ritorno un DTO con i dati dell'utente e l'access token </li>
     * </ul>
     * @param request DTO con email e password
     * @param servletRequest per accedere ai dati del client
     * @return DTO con i dati di accesso
     */
    @Transactional
    public LoginResponseDto login(@Valid AccessRequestDto request, HttpServletRequest servletRequest) {
        log.info("[LOGIN] Login in esecuzione per {}", request.getEmail());

        String userIp = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-agent");

        LoginHistoryDto loginHistoryDto = LoginHistoryDto.builder()
                .ipAddress(userIp)
                .userEmail(request.getEmail())
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .loginProvider(AuthProvider.LOCALE)
                .build();


        if (!userRepository.existsByEmail(request.getEmail())){
            log.warn("[LOGIN] Login fallito, email {} non presente nel sistema", request.getEmail());
            loginHistoryDto.setSuccess(false);
            loginHistoryDto.setFailureReason("Email non registrata");

            // Invio a kafka per il salvataggio
            sendLoginHistory(loginHistoryDto);
            throw new EmailNotRegister("Email non registrata");
        }

        // Autenticazione utente
        Authentication auth = userAuth(request, loginHistoryDto);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        if (!userDetails.getProvider().equals(AuthProvider.LOCALE)){
            log.warn("[LOGIN] Fallito per email {}, accesso già eseguito con {}", request.getEmail(), userDetails.getProvider());
            loginHistoryDto.setSuccess(false);
            loginHistoryDto.setFailureReason("Provider errato");

            // Invio a kafka per il salvataggio
            sendLoginHistory(loginHistoryDto);
            throw new WrongAuthProvider("Errore, esegui l'accesso attraverso il provider con cui ti sei registrato");
        }

        String accessToken = jwtService.generateToken(true, userDetails);

        // Generamento e salvataggio nel db del refresh-token
        String refreshToken = refreshTokenService.create(userDetails.getEmail());

        // Chiamata kafka per generare un login history
        loginHistoryDto.setSuccess(true);
        sendLoginHistory(loginHistoryDto);

        return authMapper.loginResponseDto(userDetails, accessToken);
    }


    private Authentication userAuth(AccessRequestDto request, LoginHistoryDto loginHistoryDto){
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            loginHistoryDto.setSuccess(false);
            loginHistoryDto.setFailureReason("Credenziali errate");
            sendLoginHistory(loginHistoryDto);

            log.warn("[LOGIN] Fallito per email {}: {}", request.getEmail(), ex.getMessage());
            throw ex;
        }
        return auth;
    }

    // Non chiamo subito il producer kafka nel metodo di login, altrimenti sarebbe dipendente dall'invio.
    // In questo la chiamata a producer non influenza il metodo di login con @Async
    @Async
    private void sendLoginHistory(LoginHistoryDto loginHistoryDto){
        loginHistoryProducer.sendLoginHistory(loginHistoryDto);
    }

    public String resetPassword(@Valid EmailDto email) {
        log.info("[RESET PASSWORD] Utente {} sta cercando di cambiare la password", email.getEmail());

        User user = userRepository.findByEmail(email.getEmail())
                .orElseThrow(() -> new EmailNotRegister("Utente non trovato"));

        String token = resetPassword.create(user);
    }
}
