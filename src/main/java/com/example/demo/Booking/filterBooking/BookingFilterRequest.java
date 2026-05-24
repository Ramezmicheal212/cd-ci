package com.example.demo.Booking.filterBooking;

import com.example.demo.Booking.entityBooking.BookingStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * All fields are optional. Only the ones provided will be applied as filters.
 *
 * Example GET:
 * /api/bookings/filter?status=PENDING&guestId=1&hotelId=2
 *   &checkInFrom=2026-06-01&checkInTo=2026-06-30
 *   &minAmount=100&maxAmount=500
 */
@Data
public class BookingFilterRequest {

    // Filter by booking status (PENDING / CONFIRMED / CANCELLED)
    private BookingStatus status;

    // Filter by guest
    private Long guestId;

    // Filter by hotel
    private Long hotelId;

    // Filter by room
    private Long roomId;

    // Filter by check-in date range
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInTo;

    // Filter by check-out date range
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutTo;

    // Filter by total amount range
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}