package com.example.demo.Booking.bookingMapper;

import com.example.demo.Booking.dtoBooking.Booking.BookingResponse;
import com.example.demo.Booking.dtoBooking.Booking.BookingSummary;
import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService.PolicyResult;
import com.example.demo.Booking.servicesBooking.PricingService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final CancellationPolicyService cancellationPolicyService;
    private final PricingService pricingService;

    public BookingResponse toResponse(Booking b) {

        PolicyResult policy = cancellationPolicyService.evaluate(b.getCheckInDate());

        BookingResponse r = new BookingResponse();

        // Guest
        r.setId(b.getId());
        r.setGuestId(b.getGuest().getId());
        r.setGuestName(b.getGuest().getName());
        r.setGuestEmail(b.getGuest().getEmail());

        // Hotel
        r.setHotelId(b.getHotel().getId());
        r.setHotelName(b.getHotel().getName());
        r.setHotelCity(b.getHotel().getCity());
        r.setHotelCountry(b.getHotel().getCountry());
        r.setHotelAddress(b.getHotel().getAddress());

        // Room
        r.setRoomId(b.getRoom().getId());
        r.setRoomType(b.getRoom().getRoomType());
        r.setRoomCapacity(b.getRoom().getCapacity());

        // Booking
        r.setNumberOfGuests(b.getNumberOfGuests());
        r.setCheckInDate(b.getCheckInDate());
        r.setCheckOutDate(b.getCheckOutDate());
        r.setNights(b.getNights());

        // ✅ UPDATED: استخدام calculateTotal بدل effectivePricePerNight
        r.setTotalAmount(b.getTotalAmount());
        r.setPricePerNight(
                b.getTotalAmount().divide(
                        java.math.BigDecimal.valueOf(b.getNights()),
                        2,
                        java.math.RoundingMode.HALF_UP
                )
        );

        r.setStatus(b.getStatus());
        r.setCancellationReason(b.getCancellationReason());

        // Payment info
        
        r.setRemainingBalance(b.getRemainingBalance());
        r.setMinimumRequiredPayment(b.getMinimumRequiredPayment());
        r.setPaymentDeadline(b.getPaymentDeadline());

        // Cancellation policy
        r.setRefundPolicy(policy.description());
        r.setRefundPercentage(policy.refundPercentage());

        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());

        return r;
    }

    public BookingSummary toSummary(Booking b) {

        BookingSummary s = new BookingSummary();

        s.setId(b.getId());
        s.setGuestName(b.getGuest().getName());

        // Hotel
        s.setHotelName(b.getHotel().getName());


        // Room
        s.setRoomNumber(b.getRoom().getRoomNumber());
        
        s.setNumberOfGuests(b.getNumberOfGuests());

        // Booking
        s.setCheckInDate(b.getCheckInDate());
        s.setCheckOutDate(b.getCheckOutDate());
        s.setNights(b.getNights());
        s.setTotalAmount(b.getTotalAmount());
        s.setRemainingBalance(b.getRemainingBalance());
        s.setStatus(b.getStatus());
        s.setPaymentDeadline(b.getPaymentDeadline());
        s.setCreatedAt(b.getCreatedAt());

        return s;
    }
}