package com.app.appointment.service;

import com.app.appointment.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendBookingConfirmation(Appointment appointment) {
        log.info("Booking confirmed for user {} on slot {}", appointment.getUserId(), appointment.getSlot().getId());
    }

    public void sendCancellationNotification(Appointment appointment) {
        log.info("Appointment {} cancelled by user {}", appointment.getId(), appointment.getUserId());
    }
}
