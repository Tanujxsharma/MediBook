package com.app.appointment.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SlotResponse {
    private Long id;
    private Long providerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isBooked;
}
