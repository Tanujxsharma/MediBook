package com.app.payment.controller;

import com.app.payment.dto.PaymentRequest;
import com.app.payment.dto.PaymentResponse;
import com.app.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process payment for an appointment
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    /**
     * Get payment details by payment ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentDetails(paymentId));
    }

    /**
     * Get payment by appointment ID
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PaymentResponse> getPaymentByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(paymentService.getPaymentByAppointmentId(appointmentId));
    }

    /**
     * Get all payments for current user
     */
    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(paymentService.getUserPayments(userId));
    }

    /**
     * Get all payments for a provider
     */
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<PaymentResponse>> getProviderPayments(@PathVariable Long providerId) {
        return ResponseEntity.ok(paymentService.getProviderPayments(providerId));
    }

    /**
     * Check if payment is successful
     */
    @GetMapping("/appointment/{appointmentId}/status")
    public ResponseEntity<Map<String, Boolean>> checkPaymentStatus(@PathVariable Long appointmentId) {
        boolean isSuccessful = paymentService.isPaymentSuccessful(appointmentId);
        return ResponseEntity.ok(Map.of("paymentSuccessful", isSuccessful));
    }

    /**
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId,
                                                         Authentication authentication) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}
