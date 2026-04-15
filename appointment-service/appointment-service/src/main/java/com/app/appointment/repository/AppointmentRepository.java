package com.app.appointment.repository;

import com.app.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findBySlotProviderId(Long providerId);
    List<Appointment> findBySlotId(Long slotId);
    boolean existsBySlotIdAndStatus(Long slotId, String status);
}
