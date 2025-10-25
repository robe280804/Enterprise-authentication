package com.roberto_sodini.authentication.security.config;

import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.security.UserDetailsServiceImpl;
import com.roberto_sodini.authentication.security.jwt.JwtFilter;
import com.roberto_sodini.authentication.security.oauth2.FailureHandlerImpl;
import com.roberto_sodini.authentication.security.oauth2.SuccessHandlerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final SuccessHandlerImpl successHandler;
    private final FailureHandlerImpl failureHandler;
    private final JwtFilter jwtFilter;

    /// Content-Security-Policy: default-src 'none' -> blocca il caricamento di risorse esterne come scritp
    /// X-Frame-Options: DENY -> la pagina non può esser visualizzata su altri siti attraverso un frame o iframe
    /// X-Content-Type-Options: nosniff -> se il content-type non corrisponde, non viene eseguito
    /// Strict-Transport-Security: max-age=31536000; includeSubDomains -> sito accessibile solo da https
    /// csfr -> disabilitato
    /// cors -> configurato al frontend
    /// session -> sessione stateless
    /// provider -> custom provider per recuperare l'utente
    /// jwtfilter -> filtro per validare token
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'none';")  // Prevengo attacchi XXS
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny) // Prevengo attacchi clickjacking
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                        .httpStrictTransportSecurity(hsts ->
                                hsts.includeSubDomains(true).maxAgeInSeconds(31536000) // 1 anno
                        )
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> {
                   request.requestMatchers("/api/auth/**").permitAll();
                    request.requestMatchers("/api/password/**").permitAll();
                    request.anyRequest().authenticated();
                })
                .authenticationProvider(provider())
                .oauth2Login(oauth2 -> {
                    oauth2.successHandler(successHandler);
                    oauth2.failureHandler(failureHandler);
                    oauth2.disable();

                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder());
        return provider;
    }

    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
