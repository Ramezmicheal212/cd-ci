package com.example.demo.Booking.dtoBooking.Booking;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Booking.entityBooking.BookingStatus;

@Data
public class BookingResponse {
    private Long id;

    // Guest
    private Long guestId;
    private String guestName;
    private String guestEmail;

    // Hotel
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private String hotelCountry;
    private String hotelAddress;

    // Room
    private Long roomId;
    private String roomType;
    private Integer roomCapacity;

    // Booking
    private Integer numberOfGuests;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;
    private BigDecimal pricePerNight;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String cancellationReason;

    // Payment info
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;
    private BigDecimal minimumRequiredPayment;
    private LocalDateTime paymentDeadline;

    // Cancellation policy
    private String refundPolicy;
    private int refundPercentage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}