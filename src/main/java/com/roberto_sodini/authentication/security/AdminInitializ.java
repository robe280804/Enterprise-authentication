package com.roberto_sodini.authentication.security;

import com.roberto_sodini.authentication.enums.AuthProvider;
import com.roberto_sodini.authentication.enums.Role;
import com.roberto_sodini.authentication.model.User;
import com.roberto_sodini.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializ implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@auth.com")){
            User user = User.builder()
                    .email("admin@auth.com")
                    .password(passwordEncoder.encode("Admin*123"))
                    .roles(Set.of(Role.ADMIN, Role.USER))
                    .provider(AuthProvider.LOCALE)
                    .build();

            userRepository.save(user);
            log.info("[ADMIN] Admin inizializzato");
        }
    }
}
