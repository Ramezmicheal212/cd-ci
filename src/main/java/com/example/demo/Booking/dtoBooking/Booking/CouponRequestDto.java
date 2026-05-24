package com.example.demo.Booking.dtoBooking.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CouponRequestDto(
        @NotBlank(message = "Coupon code is required")
        String code,
        @NotNull(message = "Discount type is required")
        DiscountType type,
        @NotNull(message = "Coupon value is required")
        @DecimalMin(value = "0.01", message = "Coupon value must be greater than 0")
        BigDecimal value,
        LocalDate expiryDate,
        @Positive(message = "Max usage must be greater than 0")
        Integer maxUsage
        ) {

}


                
                
                        
        
                
        


