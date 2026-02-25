package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${foody.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationCode(String toEmail, String code) {
        if (toEmail == null || toEmail.isBlank()) return;
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("Email sending skipped: mail not configured (MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM)");
            throw new IllegalArgumentException("Email sending is not configured. Set MAIL_USERNAME, MAIL_PASSWORD and MAIL_FROM before signup.");
        }

        log.info("Sending verification email to {}", toEmail);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Foody email verification code");
        message.setText("Your Foody verification code is: " + code + "\n\nThis code expires in 10 minutes.");
        try {
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}: {}", toEmail, ex.getMessage());
            throw new IllegalArgumentException("Could not send verification email. Check MAIL_USERNAME / MAIL_PASSWORD app password.");
        }
    }
}
