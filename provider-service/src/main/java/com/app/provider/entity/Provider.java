package com.app.provider.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "providers")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    private String qualification;

    private int experienceYears;

    private String bio;

    @Column(nullable = false)
    private String clinicName;

    private String clinicAddress;

    private double avgRating = 0.0;

    private boolean verified = false;

    private boolean available = true;

    @Column(updatable = false)
    private LocalDate createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDate.now();
    }
}
