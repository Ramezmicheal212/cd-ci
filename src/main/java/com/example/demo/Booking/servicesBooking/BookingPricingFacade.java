package com.example.demo.Booking.servicesBooking;
import com.example.demo.Booking.bookingRepo.CouponRepository;
import com.example.demo.Booking.bookingRepo.PricingRuleRepository;
import com.example.demo.Booking.entityBooking.Coupon;
import com.example.demo.Booking.entityBooking.PricingRule;
import com.example.demo.Booking.dtoBooking.Booking.DiscountType;
import com.example.demo.Booking.dtoBooking.Booking.RuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class BookingPricingFacade {

    private final PricingService pricingService;
    private final CouponService couponService;

    public BigDecimal calculateFinalPrice(
            BigDecimal basePrice,
            LocalDate checkIn,
            LocalDate checkOut,
            String couponCode
    ) {

        BigDecimal total = pricingService.calculateTotal(basePrice, checkIn, checkOut);

        if (couponCode != null && !couponCode.isEmpty()) {

            Coupon coupon = couponService.validate(couponCode);

            BigDecimal discount = couponService.applyDiscount(total, coupon);

            total = total.subtract(discount);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}