package com.skillsync.backend.controller;

import com.skillsync.backend.model.PasswordResetToken;
import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.PasswordResetTokenRepository;
import com.skillsync.backend.repository.UserRepository;
import com.skillsync.backend.service.EmailService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No account found with this email"));
        }

        tokenRepository.deleteByEmail(email);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        tokenRepository.save(token);

        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmailAndOtpAndUsedFalse(email, otp);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Invalid or expired OTP"));
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body(Map.of("message", "OTP has expired. Please request a new one"));
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        return ResponseEntity.ok(Map.of("message", "Password reset successful. You can now log in."));
    }
}