package com.example.demo.Booking.entityBooking;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.example.demo.Booking.dtoBooking.Booking.DayOfWeekEnum;
import com.example.demo.Booking.dtoBooking.Booking.RuleType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@RequiredArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RuleType type;

    private String name;

    private BigDecimal multiplier;

    // Months relation
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "pricing_rule_months",
            joinColumns = @JoinColumn(name = "rule_id")
    )
    @Column(name = "month_name")
    private Set<String> months = new HashSet<>();

    // Days relation
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "pricing_rule_days",
            joinColumns = @JoinColumn(name = "rule_id")
    )
    @Column(name = "day_name")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeekEnum> days = new HashSet<>();
}
