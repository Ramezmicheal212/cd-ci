package com.example.demo.Booking.dtoBooking.Booking;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayBookingRequest {

    /**
     * The amount the guest wants to pay now.
     * Must be at least 0.01.
     * To confirm: pay >= 50% of totalAmount.
     * To fully settle: pay the full remaining balance.
     */
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    private BigDecimal amount;
}
