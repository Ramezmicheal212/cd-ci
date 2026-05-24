package com.example.demo.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Controller
@RequestMapping({"/api/v1/payments", "/api/payments"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<PaymentResponseDto> makePayment(@PathVariable Long bookingId, @RequestBody PaymentRequestDto paymentRequest) {
        Payment payment = paymentService.processPayment(bookingId, paymentRequest);
        return ResponseEntity.ok(new PaymentResponseDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getPaymentMethod(),
                payment.getProvider(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getPaidAt()
        ));
    }

    @GetMapping("/{bookingId}")

    public ResponseEntity<List<PaymentResponseDto>> getPaymentsForBooking(@PathVariable Long bookingId) {
        List<Payment> payments = paymentService.getPaymentsForBooking(bookingId);
        List<PaymentResponseDto> response = payments.stream().map(payment -> new PaymentResponseDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getPaymentMethod(),
                payment.getProvider(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getPaidAt()
        )).toList();
        return ResponseEntity.ok(response);
    }

}
