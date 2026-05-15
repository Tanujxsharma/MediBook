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
    private final com.razorpay.RazorpayClient razorpayClient;

    public PaymentService(PaymentRepository paymentRepository, com.razorpay.RazorpayClient razorpayClient) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
    }

    /**
     * Create a local payment record for an appointment payment.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for appointment: {}", request.getAppointmentId());

        var existingPayment = paymentRepository.findByAppointmentId(request.getAppointmentId());
        if (existingPayment.isPresent()) {
            log.warn("Payment already exists for appointment: {}", request.getAppointmentId());
            var payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment already processed for this appointment");
            }
            return mapToResponse(payment);
        }

        Payment payment = Payment.builder()
                .appointmentId(request.getAppointmentId())
                .userId(request.getUserId())
                .providerId(request.getProviderId())
                .amount(request.getAmount())
                .status(PaymentStatus.SUCCESS)
                .transactionId("PAY-" + UUID.randomUUID())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "LOCAL")
                .description(request.getDescription())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed: {}", savedPayment.getId());
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

    public com.app.payment.dto.OrderResponse createRazorpayOrder(PaymentRequest request) throws com.razorpay.RazorpayException {
        var existingPayment = paymentRepository.findByAppointmentId(request.getAppointmentId());
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment already processed for this appointment");
        }

        double amountInPaise = request.getAmount() * 100;

        org.json.JSONObject orderRequest = new org.json.JSONObject();
        orderRequest.put("amount", (int) amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + UUID.randomUUID().toString().substring(0, 8));

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // Pre-save payment as PENDING
        if (existingPayment.isEmpty()) {
            Payment payment = Payment.builder()
                    .appointmentId(request.getAppointmentId())
                    .userId(request.getUserId())
                    .providerId(request.getProviderId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .transactionId(razorpayOrder.get("id"))
                    .paymentMethod("RAZORPAY")
                    .description(request.getDescription())
                    .build();
            paymentRepository.save(payment);
        } else {
            Payment payment = existingPayment.get();
            payment.setTransactionId(razorpayOrder.get("id"));
            paymentRepository.save(payment);
        }

        return com.app.payment.dto.OrderResponse.builder()
                .orderId(razorpayOrder.get("id"))
                .amount(request.getAmount())
                .currency("INR")
                .appointmentId(request.getAppointmentId())
                .build();
    }

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public PaymentResponse verifyPayment(com.app.payment.dto.PaymentVerifyRequest request) throws com.razorpay.RazorpayException {
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        boolean isValidSignature = com.razorpay.Utils.verifySignature(payload, request.getRazorpaySignature(), razorpaySecret);

        if (!isValidSignature) {
            throw new RuntimeException("Invalid payment signature");
        }

        Payment payment = paymentRepository.findByAppointmentId(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(request.getRazorpayPaymentId());
        paymentRepository.save(payment);

        return mapToResponse(payment);
    }
}
