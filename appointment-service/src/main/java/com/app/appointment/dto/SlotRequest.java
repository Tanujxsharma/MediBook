package com.app.appointment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SlotRequest {
    private Long providerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
