package org.Notification.service;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;

@Service
public class EmailService {

    public void sendEmail(String to, String message, @NotBlank String description) {
        System.out.println("Sending EMAIL to " + to + " : " + message);
        // later → integrate SMTP / SendGrid
    }
}