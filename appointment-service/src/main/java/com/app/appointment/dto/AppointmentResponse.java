package com.app.appointment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {
    private Long id;
    private Long userId;
    private Long slotId;
    private String status;
    private LocalDateTime createdAt;
}
