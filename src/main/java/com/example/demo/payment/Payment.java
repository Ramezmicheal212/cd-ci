package com.example.demo.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.Booking.entityBooking.Booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ Entity 

    @Table(name = "payments", indexes = {
        @Index(name = "idx_payment_booking", columnList = "booking_id"),
        @Index(name = "idx_payment_status", columnList = "status")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

    public class Payment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // ========== Relation ==========
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "booking_id", nullable = false)
        private Booking booking;

        // ========== Payment Info ==========
        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Builder.Default
        @Column(nullable = false)
        private PaymentStatus status = PaymentStatus.PENDING;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private PaymentMethod paymentMethod;

        private String transactionId;

        private String provider;

        private LocalDateTime paidAt;

        // ========== Audit ==========
        @CreationTimestamp
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        // ========== Lifecycle ==========
        @PrePersist
        @PreUpdate
        private void validateAndPopulate() {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than 0");
            }

            if (status == PaymentStatus.COMPLETED && paidAt == null) {
                paidAt = LocalDateTime.now();
            }
        }
    }

    
