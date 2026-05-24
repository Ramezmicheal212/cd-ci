package com.example.demo.Booking.servicesBooking;

import com.example.demo.Booking.bookingRepo.CouponRepository;
import com.example.demo.Booking.dtoBooking.Booking.CouponRequestDto;
import com.example.demo.Booking.entityBooking.Coupon;
import com.example.demo.Booking.dtoBooking.Booking.DiscountType;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.example.demo.ExceptionHandler.BookingException;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    @Transactional
    public Coupon validate(String code) {

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon"));

        if (coupon.getExpiryDate() != null
                && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BookingException("Coupon expired");
        }

        if (coupon.getMaxUsage() != null
                && coupon.getUsedCount() >= coupon.getMaxUsage()) {
            throw new BookingException("Coupon limit reached");
        }

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);

        return coupon;
    }

    public BigDecimal applyDiscount(BigDecimal total, Coupon coupon) {

        if (coupon.getType() == DiscountType.PERCENTAGE) {
            return total.multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100));
        }

        return coupon.getValue();
    }

    public Coupon createCoupon(
            CouponRequestDto request) {

        if (couponRepository.findByCode(request.code()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(request.code());
        coupon.setType(request.type());
        coupon.setValue(request.value());
        coupon.setExpiryDate(request.expiryDate());
        coupon.setMaxUsage(request.maxUsage());

        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<Coupon> getValidCoupons() {
        return couponRepository.findAll().stream()
                .filter(coupon -> coupon.getExpiryDate() == null || !coupon.getExpiryDate().isBefore(LocalDate.now()))
                .filter(coupon -> coupon.getMaxUsage() == null || coupon.getUsedCount() < coupon.getMaxUsage())
                .toList();
    }
}
