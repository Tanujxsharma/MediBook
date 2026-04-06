package com.app.provider.controller;

import com.app.provider.dto.ProviderRequest;
import com.app.provider.dto.ProviderResponse;
import com.app.provider.service.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ProviderResponse> add(@RequestBody ProviderRequest request) {
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

    @PutMapping("/{id}/verify")
    public ResponseEntity<ProviderResponse> verify(@PathVariable Long id) {
        return ResponseEntity.ok(service.verifyProvider(id));
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<ProviderResponse> updateRating(
            @PathVariable Long id,
            @RequestParam double rating) {
        return ResponseEntity.ok(service.updateAvgRating(id, rating));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteProvider(id);
        return ResponseEntity.ok("Provider deleted successfully");
    }
}
