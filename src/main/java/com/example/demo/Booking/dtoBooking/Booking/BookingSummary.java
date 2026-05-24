package com.example.demo.Booking.dtoBooking.Booking;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.Booking.entityBooking.BookingStatus;

@Data
public class BookingSummary {
    private Long id;
    private String guestName;

    private String hotelName;
    private String roomNumber;
    private Integer numberOfGuests;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;
    private BigDecimal totalAmount;
    private BigDecimal remainingBalance;
    private BookingStatus status;
    private LocalDateTime paymentDeadline;
    private LocalDateTime createdAt;
}