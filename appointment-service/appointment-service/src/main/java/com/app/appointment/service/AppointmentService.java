package com.app.appointment.service;

import com.app.appointment.dto.AppointmentRequest;
import com.app.appointment.dto.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookSlot(Long slotId, AppointmentRequest request);
    AppointmentResponse cancelAppointment(Long appointmentId, Long userId);
    List<AppointmentResponse> getMyAppointments(Long userId);
    List<AppointmentResponse> getProviderAppointments(Long providerId);
}
