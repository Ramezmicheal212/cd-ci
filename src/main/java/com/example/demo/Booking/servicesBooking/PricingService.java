package com.example.demo.Booking.servicesBooking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


import org.springframework.stereotype.Service;

import com.example.demo.Booking.bookingRepo.PricingRuleRepository;
import com.example.demo.Booking.dtoBooking.Booking.DayOfWeekEnum;
import com.example.demo.Booking.dtoBooking.Booking.RuleType;
import com.example.demo.Booking.entityBooking.PricingRule;

import lombok.RequiredArgsConstructor;

/**
 * Calculates the effective price per night based on: - Weekday vs Weekend
 * multiplier - Seasonal multiplier (peak / shoulder / off-peak)
 *
 * Rules: Weekend (Fri & Sat nights) → x1.25 Peak season (Jun–Aug, Dec) → x1.40
 * Shoulder (Mar–May, Sep–Nov) → x1.10 Off-peak (Jan–Feb) → x1.00
 *
 * Both multipliers stack, e.g. a Friday night in July = 1.25 * 1.40 = x1.75
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    public BigDecimal calculateTotal(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {

        List<PricingRule> rules = pricingRuleRepository.findAll();
        long totalNights = ChronoUnit.DAYS.between(checkIn, checkOut);
       
        BigDecimal total = BigDecimal.ZERO;
        LocalDate night = checkIn;
        for (int i = 0; i < totalNights; i++) {
            BigDecimal multiplier = BigDecimal.ONE;

            for (PricingRule rule : rules) {

                if (rule.getType() == RuleType.SEASON) {
                        System.out.println("Checking season rule for " + night.getMonth());
                        System.out.println("Season rule applies to months: " + rule.getMonths());
                        System.out.println("Current night is: " + night.getMonth());
                        System.out.println("Does the rule apply? " + rule.getMonths().contains(night));
                    if (rule.getMonths().contains(night.getMonth().name())) {
                        multiplier = multiplier.multiply(rule.getMultiplier());
                    }
                }

                if (rule.getType() == RuleType.WEEKEND) {
                    System.out.println("Checking weekend rule for " + night.getDayOfWeek());
                    System.out.println("Weekend rule applies to days: " + rule.getDays());
                    System.out.println("Current night is: " + night.getDayOfWeek());
                    DayOfWeekEnum nightDay = DayOfWeekEnum.valueOf(night.getDayOfWeek().name());
                    System.out.println("Does the rule apply? " + rule.getDays().contains(nightDay));
                    if (rule.getDays().contains(nightDay)) {
                        multiplier = multiplier.multiply(rule.getMultiplier());
                        System.out.println("Applied weekend multiplier for " + multiplier + " on " + night.getDayOfWeek());
                    }
                }
            }

            BigDecimal nightPrice = basePrice
                    .multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            total = total.add(nightPrice);
            night = night.plusDays(1);
        }
        System.out.println("Calculated total price: " + total + " for " + totalNights + " nights");
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
