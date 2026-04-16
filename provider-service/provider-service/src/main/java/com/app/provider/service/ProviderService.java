package com.app.provider.service;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;

import java.util.List;

public interface ProviderService {
    ProviderResponse addProvider(ProviderRequest request);
    ProviderResponse getById(Long id);
    ProviderResponse getByUserId(Long userId);
    List<ProviderResponse> getAll();
    List<ProviderResponse> getAllAdmin();
    List<ProviderResponse> search(String keyword);
    List<ProviderResponse> getBySpecialization(String specialization);
    ProviderResponse verifyProvider(Long id);
    ProviderResponse unverifyProvider(Long id);
    ProviderResponse updateMyProfile(Long userId, ProviderRequest request);
    ProviderResponse updateAvailability(Long userId, boolean available);
    boolean isBookable(Long providerId);
}
