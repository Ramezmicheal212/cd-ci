package com.example.demo.payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.Booking.entityBooking.BookingStatus;
import com.example.demo.ExceptionHandler.BookingException;
import com.example.demo.Booking.bookingRepo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import jakarta.transaction.Transactional;
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    @Transactional
    public Payment processPayment(Long bookingId,PaymentRequestDto paymentRequest) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

            if (bookingOpt.get().getStatus() == BookingStatus.CANCELLED) {
        throw new BookingException("Cannot pay cancelled booking");
   
    }
    BigDecimal remaining = bookingOpt.get().getRemainingBalance();

    if (paymentRequest.amount().compareTo(remaining) > 0) {
        throw new BookingException("Amount exceeds remaining balance");
    }



       if (paymentRequest.amount() == null || paymentRequest.amount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BookingException("Invalid payment amount");
    }


        
        Booking booking = bookingOpt.get();
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(paymentRequest.amount());
        payment.setPaymentMethod(paymentRequest.paymentMethod());
        payment.setProvider(paymentRequest.provider());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.COMPLETED); // Simulate successful payment
        payment.setPaidAt(java.time.LocalDateTime.now());   
         paymentRepository.save(payment);

            booking.getPayments().add(payment);
    if (booking.getTotalPaid()
            .compareTo(booking.getMinimumRequiredPayment()) >= 0) {

        booking.setStatus(BookingStatus.CONFIRMED);
    }


        bookingRepository.save(booking);
        return payment;
    }


   






  // get payments for booking
    public List<Payment> getPaymentsForBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }

        return paymentRepository.findByBookingId(bookingId);
    }

     

 
}