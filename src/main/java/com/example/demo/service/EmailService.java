package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender, @Value("${foody.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationCode(String toEmail, String code) {
        System.out.println(System.getenv("MAIL_PASSWORD")+(System.getenv("MAIL_USERNAME")));
        System.out.println(System.getProperty("MAIL_PASSWORD")+(System.getProperty("MAIL_USERNAME")));
        if (toEmail == null || toEmail.isBlank()) return;
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalArgumentException("Email sending is not configured. Set MAIL_USERNAME, MAIL_PASSWORD and MAIL_FROM before signup.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Foody email verification code");
        message.setText("Your Foody verification code is: " + code + "\n\nThis code expires in 10 minutes.");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new IllegalArgumentException("Could not send verification email. Check MAIL_USERNAME / MAIL_PASSWORD app password.");
        }
    }
}
