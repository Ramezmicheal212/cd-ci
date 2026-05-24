package com.example.demo.Booking.controllerBooking;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.Booking.dtoBooking.Booking.CouponRequestDto;
import com.example.demo.Booking.dtoBooking.Booking.CouponResponseDto;
import com.example.demo.Booking.entityBooking.Coupon;
import com.example.demo.Booking.servicesBooking.CouponService;
import com.example.demo.ExceptionHandler.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/coupons", "/api/coupons"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coupons", description = "Coupon management endpoints")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Create coupon", description = "Creates a coupon that can be applied to booking pricing.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Coupon created",
                content = @Content(schema = @Schema(implementation = CouponResponseDto.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid payload or duplicate coupon code",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))
        )
    })
    public ResponseEntity<CouponResponseDto> createCoupon(@Valid @RequestBody CouponRequestDto request) {
        Coupon created = couponService.createCoupon(request);

        CouponResponseDto response = new CouponResponseDto(
                created.getId(),
                created.getCode(),
                created.getType(),
                created.getValue(),
                created.getExpiryDate(),
                created.getMaxUsage(),
                created.getUsedCount()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //get coupons not expired and have usage left for a hotel
    @GetMapping("")
    public ResponseEntity<List<CouponResponseDto>> getValidCoupons() {
        List<Coupon> coupons = couponService.getValidCoupons();
        List<CouponResponseDto> response = coupons.stream().map(c -> new CouponResponseDto(
                c.getId(),
                c.getCode(),
                c.getType(),
                c.getValue(),
                c.getExpiryDate(),
                c.getMaxUsage(),
                c.getUsedCount()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

}
