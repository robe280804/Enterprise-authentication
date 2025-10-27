package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int setNewPassword(@Param("password") String password, @Param("id") UUID userId);
}
