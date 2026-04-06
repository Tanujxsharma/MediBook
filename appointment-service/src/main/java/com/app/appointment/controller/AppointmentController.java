package com.app.appointment.controller;

import com.app.appointment.dto.AppointmentRequest;
import com.app.appointment.dto.AppointmentResponse;
import com.app.appointment.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book/{slotId}")
    public ResponseEntity<AppointmentResponse> bookSlot(
            @PathVariable Long slotId,
            @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookSlot(slotId, request));
    }
}
