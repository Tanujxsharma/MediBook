package com.app.appointment.service;

import com.app.appointment.dto.SlotRequest;
import com.app.appointment.dto.SlotResponse;

import java.util.List;

public interface SlotService {
    SlotResponse createSlot(SlotRequest request);
    List<SlotResponse> getAvailableSlots(Long providerId);
}
