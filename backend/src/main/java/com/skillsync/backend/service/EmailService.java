package com.skillsync.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(@Nullable JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (mailSender == null) {
            throw new IllegalStateException("Email service is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("SkillSync AI - Password Reset OTP");
        message.setText("Your OTP to reset your SkillSync AI password is: " + otp
                + "\n\nThis OTP is valid for 10 minutes.\n\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
    }
}
