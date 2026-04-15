package com.app.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendPasswordResetNotification(String email, String token) {
        log.info("Password reset token for {} is {}", email, token);
    }
}
