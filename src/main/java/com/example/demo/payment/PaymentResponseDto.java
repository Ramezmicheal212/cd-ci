package com.example.demo.payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public record PaymentResponseDto( 
    Long id,
    Long bookingId,
    PaymentMethod paymentMethod,
    String provider,
    BigDecimal amount,
    PaymentStatus status,
    String transactionId,
    LocalDateTime paidAt
){

}

