package com.example.demo.Booking.entityBooking;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import com.example.demo.Booking.dtoBooking.Booking.*;


@Entity
@Table(name = "coupons")
@Getter
@Setter
@RequiredArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType type;

    private BigDecimal value;

    private LocalDate expiryDate;

    private Integer maxUsage;

    private Integer usedCount = 0;
}