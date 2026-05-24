package com.example.demo.Booking.bookingRepo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Booking.entityBooking.PricingRule;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
     List<PricingRule> findAll();
}
