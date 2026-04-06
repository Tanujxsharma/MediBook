package com.app.appointment.controller;

import com.app.appointment.dto.SlotRequest;
import com.app.appointment.dto.SlotResponse;
import com.app.appointment.service.SlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(@RequestBody SlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createSlot(request));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(@PathVariable Long providerId) {
        return ResponseEntity.ok(slotService.getAvailableSlots(providerId));
    }
}
