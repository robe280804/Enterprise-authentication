package com.roberto_sodini.authentication.repository;

import com.roberto_sodini.authentication.model.ResetPassword;
import com.roberto_sodini.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {
    List<ResetPassword> findAllByUser(User user);
}
