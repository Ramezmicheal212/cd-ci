package com.example.demo.Booking.bookingMapper;
import com.example.demo.Booking.dtoBooking.Booking.BookingResponse;
import com.example.demo.Booking.dtoBooking.Booking.BookingSummary;
import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService.PolicyResult;
import com.example.demo.Booking.servicesBooking.PricingService;
import com.example.demo.Booking.dtoBooking.Booking.PricingRuleResponseDto;
import com.example.demo.Booking.servicesBooking.PricingRuleService;
import com.example.demo.Booking.entityBooking.PricingRule;


import lombok.RequiredArgsConstructor;
import java.util.List;


 
public class PricingRuleMapper {

    public static PricingRuleResponseDto toDto(PricingRule rule) {
        return new PricingRuleResponseDto(
                rule.getId(),
                rule.getType(),
                rule.getName(),
                rule.getMultiplier(),
                rule.getDays(),
                rule.getMonths()
        );
    }

    
}