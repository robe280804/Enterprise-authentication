package com.roberto_sodini.authentication.service;

import com.roberto_sodini.authentication.dto.AccessRequestDto;
import com.roberto_sodini.authentication.dto.LoginResponseDto;
import com.roberto_sodini.authentication.dto.RegisterResponseDto;
import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import com.roberto_sodini.authentication.exceptions.EmailAlredyRegistered;
import com.roberto_sodini.authentication.exceptions.EmailNotRegister;
import com.roberto_sodini.authentication.exceptions.WrongAuthProvider;
import com.roberto_sodini.authentication.mapper.AuthMapper;
import com.roberto_sodini.authentication.model.EmailVerificationToken;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import com.roberto_sodini.authentication.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JwtService jwtService;
    private final EmailVerificationTokenService verificationTokenService;
    private final EmailService emailService;

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




    @Transactional
    public LoginResponseDto login(@Valid AccessRequestDto request) {
        log.info("[LOGIN] Login in esecuzione per {}", request.getEmail());

        if (!userRepository.existsByEmail(request.getEmail())){
            log.warn("[LOGIN] Login fallito, email {} non presente nel sistema", request.getEmail());
            throw new EmailNotRegister("Email non registrata");
        }

        Authentication auth = userAuth(request);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        if (!userDetails.getProvider().equals(AuthProvider.LOCALE)){
            log.warn("[LOGIN] Fallito per email {}, accesso già eseguito con {}", request.getEmail(), userDetails.getProvider());
            throw new WrongAuthProvider("Errore, esegui l'accesso attraverso il provider con cui ti sei registrato");
        }

        String accessToken = jwtService.generateToken(true, userDetails);
        String refreshToken = jwtService.generateToken(false, userDetails);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);  //js non può leggerlo
        //refreshTokenCookie.setSecure(true);  solo HTTPS
        refreshTokenCookie.setPath("/api/auth/refresh");
        refreshTokenCookie.setMaxAge(Math.toIntExact(longExpiration));

        return authMapper.loginResponseDto(userDetails, accessToken);

    }


    private Authentication userAuth(AccessRequestDto request){
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("[LOGIN] Fallito per email {}: {}", request.getEmail(), ex.getMessage());
            throw ex;
        }
        return auth;
    }

}
