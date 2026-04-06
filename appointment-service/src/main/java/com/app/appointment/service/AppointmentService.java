package com.app.appointment.service;

import com.app.appointment.dto.AppointmentRequest;
import com.app.appointment.dto.AppointmentResponse;

public interface AppointmentService {
    AppointmentResponse bookSlot(Long slotId, AppointmentRequest request);
}
