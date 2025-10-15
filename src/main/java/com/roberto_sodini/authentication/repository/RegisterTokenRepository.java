package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.RegisterToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterTokenRepository extends JpaRepository<RegisterToken, Long> {
}
