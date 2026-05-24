package com.example.demo.Booking.dtoBooking.Booking;

import lombok.Data;

@Data
public class CancelBookingRequest {
    private String reason; // optional cancellation reason
}

