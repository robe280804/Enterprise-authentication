package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.RefreshToken;
import com.roberto_sodini.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByUser(User user);

    @Query("SELECT r FROM RefreshToken r WHERE r.user = :user AND r.revoked = false")
    List<RefreshToken> findAllByUserAndNonRevoked(@Param("user") User user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.email = :email")
    void revokedAllForUserEmail(@Param("email") String email);
}
