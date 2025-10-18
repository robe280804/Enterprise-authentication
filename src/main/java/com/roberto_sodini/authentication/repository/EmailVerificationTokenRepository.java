package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findAllByUserEmail(String userEmail);
}
