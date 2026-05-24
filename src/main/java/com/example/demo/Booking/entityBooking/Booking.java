package com.example.demo.Booking.entityBooking;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.catalog.entity.Guest;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.Room;
import com.example.demo.payment.Payment;
import com.example.demo.payment.PaymentMethod;
import com.example.demo.payment.PaymentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_guest", columnList = "guest_id"),
        @Index(name = "idx_booking_hotel", columnList = "hotel_id"),
        @Index(name = "idx_booking_room", columnList = "room_id"),
        @Index(name = "idx_booking_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= Relations =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // ================= Booking Details =================

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // ================= Pricing =================

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumRequiredPayment;

    // ================= Payment Deadline =================

    @Column(nullable = false)
    private LocalDateTime paymentDeadline;

    // ================= Payments =================

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true , fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    // ================= Cancellation =================

    private String cancellationReason;

    // ================= Audit =================

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ================= Business Logic =================
    
@PrePersist
private void prePersist() {
    initDefaults();
}

private void initDefaults() {

    if (status == null) {
        status = BookingStatus.PENDING;
    }

    if (totalAmount == null) {
        throw new IllegalStateException("totalAmount must be set before saving booking");
    }

    if (minimumRequiredPayment == null) {
        minimumRequiredPayment = BigDecimal.ZERO;
    }

    if (paymentDeadline == null) {
        paymentDeadline = LocalDateTime.now().plusMinutes(30);
    }
}

// ================= Business Logic =================

public BigDecimal getTotalPaid() {
    if (payments == null || payments.isEmpty()) {
        return BigDecimal.ZERO;
    }
    return payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}

public BigDecimal getRemainingBalance() {
    return totalAmount.subtract(getTotalPaid());
}

public boolean isFullyPaid() {
    return getTotalPaid().compareTo(totalAmount) >= 0;
}

public boolean isPendingPayment() {
    return getTotalPaid().compareTo(minimumRequiredPayment) < 0;
}

public boolean isPaymentExpired() {
    return LocalDateTime.now().isAfter(paymentDeadline);
}

// ================= Status Logic (FIXED) =================

public BookingStatus resolveStatus() {

    if (isFullyPaid()) {
        return BookingStatus.CONFIRMED;
    }

    if (isPaymentExpired() && !isFullyPaid()) {
        return BookingStatus.CANCELLED;
    }

    return BookingStatus.PENDING;
}

// ================= Nights =================

public long getNights() {
    return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
}
}