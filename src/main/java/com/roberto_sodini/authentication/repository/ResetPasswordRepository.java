package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.RegistrationVerification;
import com.roberto_sodini.authentication.model.ResetPassword;
import com.roberto_sodini.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {

    Optional<ResetPassword> findByToken(String token);

    @Modifying
    @Query("UPDATE ResetPassword rp SET rp.revoked = true WHERE rp.user = :user")
    void revokedAllByUser(@Param("user") User user);

    @Query("SELECT rp FROM ResetPassword rp " +
            "WHERE rp.token = :token " +
            "AND rp.revoked = false " +
            "AND rp.expiryDate >= :now")
    Optional<ResetPassword> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE ResetPassword rv " +
            "SET rv.revoked = true " +
            "WHERE rv.id = :id")
    int revokedAndConfirmToken(@Param("id") Long id);

}
