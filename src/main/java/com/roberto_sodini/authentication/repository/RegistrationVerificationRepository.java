package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.RegistrationVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RegistrationVerificationRepository extends JpaRepository<RegistrationVerification, Long> {

    @Modifying
    @Query("UPDATE RegistrationVerification rv SET rv.revoked = true " +
            "WHERE rv.userEmail = :userEmail ")
    void revokedAllByUserEmail(@Param("userEmail") String userEmail);

    @Query("SELECT rv FROM RegistrationVerification rv " +
            "WHERE rv.token = :token " +
            "AND rv.revoked = false " +
            "AND rv.expiryDate >= :now")
    Optional<RegistrationVerification> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RegistrationVerification rv " +
            "SET rv.revoked = true, " +
            "rv.registerSuccess = true " +
            "WHERE rv.id = :id")
    int revokedAndConfirmToken(@Param("id") Long id);
}
