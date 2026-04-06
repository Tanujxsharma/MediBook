package com.app.provider.dto;

import lombok.Data;

@Data
public class ProviderRequest {
    private Long userId;
    private String name;
    private String specialization;
    private String qualification;
    private int experienceYears;
    private String bio;
    private String clinicName;
    private String clinicAddress;
}
