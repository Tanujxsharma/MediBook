package com.app.provider.service;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;

import java.util.List;

public interface ProviderService {

    ProviderResponse addProvider(ProviderRequest request);

    ProviderResponse getById(Long id);

    List<ProviderResponse> getAll();

    List<ProviderResponse> search(String keyword);

    List<ProviderResponse> getBySpecialization(String specialization);

    ProviderResponse verifyProvider(Long id);

    ProviderResponse updateAvgRating(Long id, double rating);

    void deleteProvider(Long id);
}
