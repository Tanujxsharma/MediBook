package com.app.appointment.service;

import com.app.appointment.dto.SlotRequest;
import com.app.appointment.dto.SlotResponse;
import com.app.appointment.entity.Slot;
import com.app.appointment.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;

    public SlotServiceImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public SlotResponse createSlot(SlotRequest request) {
        Slot slot = Slot.builder()
                .providerId(request.getProviderId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();
                
        slot = slotRepository.save(slot);
        return mapToDto(slot);
    }

    @Override
    public List<SlotResponse> getAvailableSlots(Long providerId) {
        return slotRepository.findByProviderIdAndIsBookedFalse(providerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SlotResponse mapToDto(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .providerId(slot.getProviderId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.isBooked())
                .build();
    }
}
