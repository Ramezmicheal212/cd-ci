package com.example.demo.Booking.dtoBooking.Booking;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import com.example.demo.Booking.dtoBooking.Booking.RuleType;
 
public record PricingRuleResponseDto(
    Long id,
    RuleType type,
    String description,
    BigDecimal multiplier,
    Set<DayOfWeekEnum> dayOfWeek,
    Set<String> month
) {}