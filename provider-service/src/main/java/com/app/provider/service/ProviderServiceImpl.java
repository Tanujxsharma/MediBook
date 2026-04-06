package com.app.provider.service;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;
import com.app.provider.entity.Provider;
import com.app.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository repo;

    public ProviderServiceImpl(ProviderRepository repo) {
        this.repo = repo;
    }

    @Override
    public ProviderResponse addProvider(ProviderRequest request) {
        Provider provider = Provider
                .builder()
                .userId(request.getUserId())
                .name(request.getName())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .clinicName(request.getClinicName())
                .clinicAddress(request.getClinicAddress())
                .verified(false)
                .available(true)
                .avgRating(0.0)
                .build();

        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse getById(Long id) {
        Provider provider = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + id));
        return mapToResponse(provider);
    }

    @Override
    public List<ProviderResponse> getAll() {
        return repo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderResponse> search(String keyword) {
        return repo.searchByNameOrSpecialization(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderResponse> getBySpecialization(String specialization) {
        return repo.findBySpecialization(specialization).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProviderResponse verifyProvider(Long id) {
        Provider provider = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + id));
        provider.setVerified(true);
        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse updateAvgRating(Long id, double newRating) {
        Provider provider = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + id));
        provider.setAvgRating(newRating);
        return mapToResponse(repo.save(provider));
    }

    @Override
    public void deleteProvider(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Provider not found with id: " + id);
        }
        repo.deleteById(id);
    }

    private ProviderResponse mapToResponse(Provider p) {
        return ProviderResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .name(p.getName())
                .specialization(p.getSpecialization())
                .qualification(p.getQualification())
                .experienceYears(p.getExperienceYears())
                .bio(p.getBio())
                .clinicName(p.getClinicName())
                .clinicAddress(p.getClinicAddress())
                .avgRating(p.getAvgRating())
                .verified(p.isVerified())
                .available(p.isAvailable())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
