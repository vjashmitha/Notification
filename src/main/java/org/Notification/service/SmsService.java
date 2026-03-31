package org.Notification.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendSms(String phone, String message) {
        System.out.println("Sending SMS to " + phone + " : " + message);
        // later → integrate Twilio
    }
}
