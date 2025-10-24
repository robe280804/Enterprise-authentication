package com.roberto_sodini.authentication.security.oauth2;

import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.UserRepository;
import com.roberto_sodini.authentication.security.UserDetailsImpl;
import com.roberto_sodini.authentication.security.UserDetailsServiceImpl;
import com.roberto_sodini.authentication.security.jwt.JwtService;
import com.roberto_sodini.authentication.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Email del client
        String email = oAuth2User.getAttribute("email");
        String userEmail;
        if (provider.equalsIgnoreCase("google")){
            userEmail = (email != null) ? email : UUID.randomUUID().toString() + "login@google.com";
        } else {
            userEmail = (email != null) ? email : UUID.randomUUID().toString() + "login@github.com";
        }

        log.info("[OAUTH2 SUCCESS HANDLER] Accesso eseguito con successo dal provider {} con email {}", provider, userEmail );

        // Ottengo l'utente e se non esiste lo creo
        UserDetailsImpl userDetails = userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)
                .orElseGet(() -> {
                    User newUser = userRepository.save(User.builder()
                            .email(userEmail)
                            .provider((provider.equalsIgnoreCase("google") ? AuthProvider.GOOGLE : AuthProvider.GITHUB))
                            .password(null)
                            .roles(Set.of(Role.USER))
                            .build());

                    return new UserDetailsImpl(newUser);
                });

        // Genero access-token e refresh-token
        String accessToken = jwtService.generateToken(true, userDetails);
        String refreshToken = refreshTokenService.create(userDetails.getEmail());


        // Redirect al frontend
        String redirectUrl = "http://localhost:5173/?token=" + accessToken;

        log.info("[OAUTH2 SUCCESS HANDLER] Creazione del token andata a buon fine");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.sendRedirect(redirectUrl);

    }
}
