package com.app.appointment.service;

import com.app.appointment.dto.SlotRequest;
import com.app.appointment.dto.SlotResponse;
import com.app.appointment.repository.AppointmentRepository;
import com.app.appointment.entity.Slot;
import com.app.appointment.repository.SlotRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public SlotServiceImpl(SlotRepository slotRepository, AppointmentRepository appointmentRepository) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public SlotResponse createSlot(SlotRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot create slots in the past");
        }

        Boolean providerBookable = restTemplate.getForObject(
                "http://localhost:8082/providers/internal/" + request.getProviderId() + "/bookable",
                Boolean.class
        );

        if (providerBookable == null || !providerBookable) {
            throw new RuntimeException("Provider is not available for booking");
        }

        boolean overlap = slotRepository.existsByProviderIdAndStartTimeLessThanAndEndTimeGreaterThan(
                request.getProviderId(),
                request.getEndTime(),
                request.getStartTime()
        );

        if (overlap) {
            throw new RuntimeException("Overlapping slot already exists");
        }

        Slot slot = Slot.builder()
                .providerId(request.getProviderId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        return mapToResponse(slotRepository.save(slot));
    }

    @Override
    public List<SlotResponse> getAvailableSlots(Long providerId) {
        Boolean providerBookable = restTemplate.getForObject(
                "http://localhost:8082/providers/internal/" + providerId + "/bookable",
                Boolean.class
        );

        if (providerBookable == null || !providerBookable) {
            return Collections.emptyList();
        }

        return slotRepository.findByProviderIdAndIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(
                        providerId,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SlotResponse> getAllSlotsOfProvider(Long providerId) {
        return slotRepository.findByProviderId(providerId)
                .stream()
                .sorted(Comparator.comparing(Slot::getStartTime))
                .map(this::mapToResponseUnfiltered)
                .collect(Collectors.toList());
    }

    @Override
    public List<SlotResponse> getPublicAvailableSlots(LocalDate date, String specialization, String search) {
        List<Slot> slots = slotRepository.findByIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());

        return slots.stream()
                .map(slot -> enrichSlot(slot))
                .filter(Objects::nonNull)
                .filter(slot -> {
                    if (date == null) return true;
                    return slot.getStartTime().toLocalDate().equals(date);
                })
                .filter(slot -> {
                    if (specialization == null || specialization.isBlank()) return true;
                    return slot.getSpecialization() != null
                            && slot.getSpecialization().equalsIgnoreCase(specialization.trim());
                })
                .filter(slot -> {
                    if (search == null || search.isBlank()) return true;
                    String term = search.toLowerCase();
                    return (slot.getDoctorName() != null && slot.getDoctorName().toLowerCase().contains(term)) ||
                           (slot.getSpecialization() != null && slot.getSpecialization().toLowerCase().contains(term)) ||
                           (slot.getClinicName() != null && slot.getClinicName().toLowerCase().contains(term));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long providerId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getProviderId().equals(providerId)) {
            throw new RuntimeException("You do not have permission to delete this slot");
        }

        if (slot.isBooked() || appointmentRepository.existsBySlotIdAndStatus(slotId, "BOOKED")) {
            throw new RuntimeException("Cannot delete a booked slot");
        }

        appointmentRepository.deleteAll(appointmentRepository.findBySlotId(slotId));
        slotRepository.delete(slot);
    }

    private SlotResponse mapToResponse(Slot slot) {
        return enrichSlot(slot);
    }

    private SlotResponse enrichSlot(Slot slot) {
        try {
            Boolean providerBookable = restTemplate.getForObject(
                    "http://localhost:8082/providers/internal/" + slot.getProviderId() + "/bookable",
                    Boolean.class
            );

            if (providerBookable == null || !providerBookable) {
                return null;
            }

            return mapToResponseUnfiltered(slot);
        } catch (Exception ex) {
            return null;
        }
    }

    private SlotResponse mapToResponseUnfiltered(Slot slot) {
        try {
            Map<?, ?> provider = restTemplate.getForObject(
                    "http://localhost:8082/providers/" + slot.getProviderId(),
                    Map.class
            );

            String doctorName = provider != null ? Objects.toString(provider.get("name"), "") : "";
            String specialization = provider != null ? Objects.toString(provider.get("specialization"), "") : "";
            String clinicName = provider != null ? Objects.toString(provider.get("clinicName"), "") : "";
            double minimumFees = 0.0;
            if (provider != null && provider.get("minimumFees") != null) {
                try {
                    minimumFees = Double.parseDouble(provider.get("minimumFees").toString());
                } catch (NumberFormatException e) {
                    minimumFees = 0.0;
                }
            }

            return SlotResponse.builder()
                    .id(slot.getId())
                    .providerId(slot.getProviderId())
                    .doctorName(doctorName)
                    .specialization(specialization)
                    .clinicName(clinicName)
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .isBooked(slot.isBooked())
                    .minimumFees(minimumFees)
                    .build();
        } catch (Exception ex) {
            return SlotResponse.builder()
                    .id(slot.getId())
                    .providerId(slot.getProviderId())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .isBooked(slot.isBooked())
                    .minimumFees(0.0)
                    .build();
        }
    }
}
