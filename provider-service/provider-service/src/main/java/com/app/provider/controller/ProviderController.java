package com.app.provider.controller;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;
import com.app.provider.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService service;

    public ProviderController(ProviderService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ResponseEntity<ProviderResponse> add(@Valid @RequestBody ProviderRequest request,
                                                Authentication authentication) {
        Long loggedInUserId = (Long) authentication.getPrincipal();
        request.setUserId(loggedInUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addProvider(request));
    }

    @GetMapping
    public ResponseEntity<List<ProviderResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProviderResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @GetMapping("/specialization")
    public ResponseEntity<List<ProviderResponse>> getBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(service.getBySpecialization(name));
    }

    @GetMapping("/me")
    public ResponseEntity<ProviderResponse> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProviderResponse> updateMyProfile(@Valid @RequestBody ProviderRequest request,
                                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.updateMyProfile(userId, request));
    }

    @PutMapping("/me/availability")
    public ResponseEntity<ProviderResponse> updateAvailability(@RequestParam boolean available,
                                                               Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(service.updateAvailability(userId, available));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<ProviderResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(service.verifyProvider(id));
    }

    @PutMapping("/{id}/unverify")
    public ResponseEntity<ProviderResponse> unverify(@PathVariable Long id) {
        return ResponseEntity.ok(service.unverifyProvider(id));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ProviderResponse>> getAllAdmin() {
        return ResponseEntity.ok(service.getAllAdmin());
    }

    @GetMapping("/internal/by-user/{userId}")
    public ResponseEntity<ProviderResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping("/internal/{providerId}/bookable")
    public ResponseEntity<Boolean> isBookable(@PathVariable Long providerId) {
        return ResponseEntity.ok(service.isBookable(providerId));
    }
}
