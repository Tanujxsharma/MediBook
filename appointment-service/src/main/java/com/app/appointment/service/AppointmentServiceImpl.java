package com.app.appointment.service;

import com.app.appointment.dto.AppointmentRequest;
import com.app.appointment.dto.AppointmentResponse;
import com.app.appointment.entity.Appointment;
import com.app.appointment.entity.Slot;
import com.app.appointment.repository.AppointmentRepository;
import com.app.appointment.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, SlotRepository slotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    @Transactional
    public AppointmentResponse bookSlot(Long slotId, AppointmentRequest request) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot is already booked");
        }

        // Optimistic locking handles concurrent modifications.
        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .userId(request.getUserId())
                .slot(slot)
                .status("BOOKED")
                .build();

        appointment = appointmentRepository.save(appointment);

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .userId(appointment.getUserId())
                .slotId(slot.getId())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
