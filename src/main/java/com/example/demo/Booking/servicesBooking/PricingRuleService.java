package com.example.demo.Booking.servicesBooking;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.demo.Booking.bookingRepo.*;
import com.example.demo.Booking.entityBooking.*;
import com.example.demo.Booking.dtoBooking.Booking.*;
import com.example.demo.Booking.dtoBooking.Booking.BookingResponse;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService;
import com.example.demo.Booking.dtoBooking.Booking.PricingRuleResponseDto;
import com.example.demo.Booking.bookingMapper.PricingRuleMapper;

import java.util.List;
import java.math.BigDecimal;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
     
    public PricingRuleResponseDto  getRuleById(Long id) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing rule not found"));

        return  PricingRuleMapper.toDto(rule);
        
    
         
    }

    public List<PricingRuleResponseDto> getAllPricingRules() {
        List<PricingRule> rules = pricingRuleRepository.findAll();
        return rules.stream()
                .map(PricingRuleMapper::toDto)
                .toList();
    }

    public PricingRuleResponseDto createRule(PricingRuleRequestDto request) {
        PricingRule rule = new PricingRule();
        rule.setType(request.ruleType());
        rule.setName(request.name());
        rule.setMultiplier(request.multiplier());
        rule.setMonths(request.months());
        rule.setDays(request.days());

        PricingRule saved = pricingRuleRepository.save(rule);
        return PricingRuleMapper.toDto(saved);
    }
}
