package com.example.demo.Booking.dtoBooking.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CouponResponseDto(
        Long id,
        String code,
        DiscountType type,
        BigDecimal value,
        LocalDate expiryDate,
        Integer maxUsage,
        Integer usedCount
        ) {

}


        
        
        
        
        
        
        
        
