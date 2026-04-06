package com.app.provider.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProviderResponse {
    private Long id;
    private Long userId;
    private String name;
    private String specialization;
    private String qualification;
    private int experienceYears;
    private String bio;
    private String clinicName;
    private String clinicAddress;
    private double avgRating;
    private boolean verified;
    private boolean available;
    private LocalDate createdAt;
}
