package com.app.appointment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Column(nullable = false)
    private String status; // e.g., "BOOKED", "CANCELLED", "COMPLETED"

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
