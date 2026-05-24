package com.example.demo.Booking.bookingRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.example.demo.Booking.entityBooking.Coupon;


public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
}