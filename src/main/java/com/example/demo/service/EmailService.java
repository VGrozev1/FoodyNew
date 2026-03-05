package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private final String fromAddress;
    private final String brevoApiKey;
    private final HttpClient httpClient;

    public EmailService(
            @Value("${foody.mail.from:}") String fromAddress,
            @Value("${foody.brevo.api-key:}") String brevoApiKey
    ) {
        this.fromAddress = fromAddress;
        this.brevoApiKey = brevoApiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendVerificationCode(String toEmail, String code) {
        if (toEmail == null || toEmail.isBlank()) return;
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("Email sending skipped: foody.mail.from is not configured");
            throw new IllegalArgumentException("Email sending is not configured. Set MAIL_FROM before signup.");
        }
        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            log.warn("Email sending skipped: BREVO_API_KEY (foody.brevo.api-key) is not configured");
            throw new IllegalArgumentException("Email sending is not configured. Set BREVO_API_KEY before signup.");
        }

        String subject = "Foody email verification code";
        String text = "Your Foody verification code is: " + code + "\n\nThis code expires in 10 minutes.";

        String jsonBody = "{"
                + "\"sender\":{\"email\":\"" + escapeJson(fromAddress) + "\",\"name\":\"Foody\"},"
                + "\"to\":[{\"email\":\"" + escapeJson(toEmail) + "\"}],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"textContent\":\"" + escapeJson(text) + "\""
                + "}";

        log.info("Sending verification email via Brevo API to {}", toEmail);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_ENDPOINT))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                log.info("Verification email sent successfully to {} (status {})", toEmail, status);
            } else {
                log.error("Failed to send verification email to {}: status {}, body {}", toEmail, status, response.body());
                throw new IllegalArgumentException("Could not send verification email. Email provider rejected the request.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Brevo API call interrupted for {}: {}", toEmail, e.getMessage());
            throw new IllegalArgumentException("Could not send verification email. Email provider is unreachable.");
        } catch (IOException e) {
            log.error("Error calling Brevo API for {}: {}", toEmail, e.getMessage());
            throw new IllegalArgumentException("Could not send verification email. Email provider is unreachable.");
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
