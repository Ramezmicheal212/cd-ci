package com.example.demo.Booking.dtoBooking.Booking;
import java.util.Set;
import com.example.demo.Booking.dtoBooking.Booking.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PricingRuleRequestDto(
    @NotNull(message = "Rule type cannot be null")
    RuleType ruleType,
    @NotBlank(message = "Rule name cannot be null")
    String name,
    @NotNull(message = "Multiplier cannot be null")
    BigDecimal multiplier,
    Set<String> months,
    Set<DayOfWeekEnum> days


) {

}
