package com.app.payment.service;

import com.app.payment.dto.PaymentRequest;
import com.app.payment.dto.PaymentResponse;
import com.app.payment.entity.Payment;
import com.app.payment.entity.PaymentStatus;
import com.app.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process a demo payment for appointment
     * In production, this would integrate with Stripe, PayPal, etc.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for appointment: {}", request.getAppointmentId());

        // Check if payment already exists for this appointment
        var existingPayment = paymentRepository.findByAppointmentId(request.getAppointmentId());
        if (existingPayment.isPresent()) {
            log.warn("Payment already exists for appointment: {}", request.getAppointmentId());
            throw new RuntimeException("Payment already processed for this appointment");
        }

        Payment payment = Payment.builder()
                .appointmentId(request.getAppointmentId())
                .userId(request.getUserId())
                .providerId(request.getProviderId())
                .amount(request.getAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "DEMO")
                .description(request.getDescription())
                .build();

        // Generate transaction ID
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString());

        // Demo payment flow always succeeds so booking UX stays deterministic.
        payment.setStatus(PaymentStatus.SUCCESS);
        log.info("Demo payment SUCCESS: {}", payment.getTransactionId());

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    /**
     * Get payment by appointment ID
     */
    public PaymentResponse getPaymentByAppointmentId(Long appointmentId) {
        var payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for appointment: " + appointmentId));
        return mapToResponse(payment);
    }

    /**
     * Get all payments for a user
     */
    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all payments for a provider
     */
    public List<PaymentResponse> getProviderPayments(Long providerId) {
        return paymentRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get payment details
     */
    public PaymentResponse getPaymentDetails(Long paymentId) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * Refund a payment
     */
    public PaymentResponse refundPayment(Long paymentId) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Can only refund successful payments");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);
        log.info("Payment refunded: {}", refundedPayment.getId());
        return mapToResponse(refundedPayment);
    }

    /**
     * Check if payment is successful
     */
    public boolean isPaymentSuccessful(Long appointmentId) {
        var payment = paymentRepository.findByAppointmentId(appointmentId);
        return payment.isPresent() && payment.get().getStatus() == PaymentStatus.SUCCESS;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .appointmentId(payment.getAppointmentId())
                .userId(payment.getUserId())
                .providerId(payment.getProviderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
