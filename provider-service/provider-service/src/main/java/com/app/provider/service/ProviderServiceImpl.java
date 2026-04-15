package com.app.provider.service;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;
import com.app.provider.entity.Provider;
import com.app.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository repo;
    private final RestTemplate restTemplate = new RestTemplate();

    public ProviderServiceImpl(ProviderRepository repo) {
        this.repo = repo;
    }

    @Override
    public ProviderResponse addProvider(ProviderRequest request) {
        if (repo.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Provider profile already exists for this doctor");
        }

        Provider provider = Provider.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .clinicName(request.getClinicName())
                .clinicAddress(request.getClinicAddress())
                .avgRating(0.0)
                .minimumFees(request.getMinimumFees())
                .verified(false)
                .available(true)
                .build();

        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse getById(Long id) {
        return mapToResponse(repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found")));
    }

    @Override
    public ProviderResponse getByUserId(Long userId) {
        return mapToResponse(repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for this doctor")));
    }

    @Override
    public List<ProviderResponse> getAll() {
        return repo.findAll().stream()
                .filter(Provider::isVerified)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderResponse> getAllAdmin() {
        return repo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderResponse> search(String keyword) {
        return repo.searchByNameOrSpecialization(keyword).stream()
                .filter(Provider::isVerified)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderResponse> getBySpecialization(String specialization) {
        return repo.findBySpecialization(specialization).stream()
                .filter(Provider::isVerified)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProviderResponse verifyProvider(Long id) {
        Provider provider = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setVerified(true);
        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse unverifyProvider(Long id) {
        Provider provider = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setVerified(false);
        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse updateMyProfile(Long userId, ProviderRequest request) {
        Provider provider = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for this doctor"));

        provider.setName(request.getName());
        provider.setSpecialization(request.getSpecialization());
        provider.setQualification(request.getQualification());
        provider.setExperienceYears(request.getExperienceYears());
        provider.setBio(request.getBio());
        provider.setClinicName(request.getClinicName());
        provider.setClinicAddress(request.getClinicAddress());
        provider.setMinimumFees(request.getMinimumFees());

        return mapToResponse(repo.save(provider));
    }

    @Override
    public ProviderResponse updateAvailability(Long userId, boolean available) {
        Provider provider = repo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for this doctor"));

        provider.setAvailable(available);
        return mapToResponse(repo.save(provider));
    }

    @Override
    public boolean isBookable(Long providerId) {
        Provider provider = repo.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return provider.isVerified() && provider.isAvailable();
    }

    private ProviderResponse mapToResponse(Provider p) {
        String email = "";
        try {
            Map<?, ?> user = restTemplate.getForObject(
                    "http://localhost:8083/users/internal/" + p.getUserId(),
                    Map.class
            );
            email = user != null && user.get("email") != null ? user.get("email").toString() : "";
        } catch (Exception ignored) {
            email = "";
        }

        return ProviderResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .name(p.getName())
                .email(email)
                .specialization(p.getSpecialization())
                .qualification(p.getQualification())
                .experienceYears(p.getExperienceYears())
                .bio(p.getBio())
                .clinicName(p.getClinicName())
                .clinicAddress(p.getClinicAddress())
                .avgRating(p.getAvgRating())
                .minimumFees(p.getMinimumFees())
                .verified(p.isVerified())
                .available(p.isAvailable())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
