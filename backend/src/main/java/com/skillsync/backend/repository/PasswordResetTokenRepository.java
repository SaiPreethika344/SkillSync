package com.skillsync.backend.repository;

import com.skillsync.backend.model.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndOtpAndUsedFalse(String email, String otp);

    void deleteByEmail(String email);
}