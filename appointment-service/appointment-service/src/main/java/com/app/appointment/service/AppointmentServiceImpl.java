package com.app.appointment.service;

import com.app.appointment.dto.AppointmentRequest;
import com.app.appointment.dto.AppointmentResponse;
import com.app.appointment.entity.Appointment;
import com.app.appointment.entity.Slot;
import com.app.appointment.repository.AppointmentRepository;
import com.app.appointment.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  SlotRepository slotRepository,
                                  NotificationService notificationService,
                                  RestTemplate restTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public AppointmentResponse bookSlot(Long slotId, AppointmentRequest request) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot is already booked");
        }

        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book past slots");
        }

        Boolean providerBookable = restTemplate.getForObject(
                "http://localhost:8082/providers/internal/" + slot.getProviderId() + "/bookable",
                Boolean.class
        );

        if (providerBookable == null || !providerBookable) {
            throw new RuntimeException("This doctor is not verified yet. Please book with a verified doctor.");
        }

        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .userId(request.getUserId())
                .slot(slot)
                .status("BOOKED")
                .notes(request.getNotes())
                .build();

        appointment = appointmentRepository.save(appointment);
        notificationService.sendBookingConfirmation(appointment);

        return mapToResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("You can cancel only your own appointment");
        }

        if (!"BOOKED".equals(appointment.getStatus())) {
            throw new RuntimeException("Only booked appointments can be cancelled");
        }

        appointment.setStatus("CANCELLED");

        Slot slot = appointment.getSlot();
        slot.setBooked(false);
        slotRepository.save(slot);

        Appointment saved = appointmentRepository.save(appointment);
        notificationService.sendCancellationNotification(saved);

        return mapToResponse(saved);
    }

    @Override
    public List<AppointmentResponse> getMyAppointments(Long userId) {
        return appointmentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getProviderAppointments(Long providerId) {
        return appointmentRepository.findBySlotProviderId(providerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        Slot slot = appointment.getSlot();
        
        // Fetch patient name from auth service
        String patientName = "Patient";
        try {
            Object userResponse = restTemplate.getForObject(
                    "http://localhost:8083/users/internal/" + appointment.getUserId(),
                    Object.class
            );
            if (userResponse instanceof java.util.Map) {
                Object name = ((java.util.Map) userResponse).get("name");
                if (name != null) {
                    patientName = name.toString();
                }
            }
        } catch (Exception e) {
            // If we can't fetch patient name, use default
            System.err.println("Could not fetch patient name for user: " + appointment.getUserId());
        }

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUserId())
                .patientName(patientName)
                .slotId(slot.getId())
                .providerId(slot.getProviderId())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .slotStartTime(slot.getStartTime())
                .slotEndTime(slot.getEndTime())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
