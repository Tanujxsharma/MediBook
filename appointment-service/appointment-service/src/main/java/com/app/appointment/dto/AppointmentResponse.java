package com.app.appointment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long userId;
    private String patientName;
    private Long slotId;
    private Long providerId;
    private String status;
    private String notes;
    private LocalDateTime slotStartTime;
    private LocalDateTime slotEndTime;
    private LocalDateTime createdAt;
}
