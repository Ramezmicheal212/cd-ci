package com.example.demo.Booking.controllerBooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Booking.dtoBooking.Booking.BookingResponse;
import com.example.demo.Booking.dtoBooking.Booking.BookingSummary;
import com.example.demo.Booking.dtoBooking.Booking.CancelBookingRequest;
import com.example.demo.Booking.dtoBooking.Booking.CreateBookingRequest;
import com.example.demo.Booking.dtoBooking.Booking.PayBookingRequest;
import com.example.demo.Booking.entityBooking.BookingStatus;
import com.example.demo.Booking.filterBooking.BookingFilterRequest;
import com.example.demo.Booking.servicesBooking.BookingService;
import com.example.demo.ExceptionHandler.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({"/api/v1/bookings", "/api/bookings"})
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bookings", description = "Booking lifecycle and pricing endpoints")
public class BookingController {

    private final BookingService bookingService;

    // CREATE
    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a booking and returns pricing details.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Booking created",
                content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Guest, room, or hotel not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET SINGLE
    @GetMapping("/{id}")
    @Operation(summary = "Get booking", description = "Returns booking details by booking id.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking returned",
                content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    // PAY
    /*@PostMapping("/{id}/pay")
    @Operation(summary = "Pay booking", description = "Processes payment for a booking.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking paid",
                content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payment request",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> payBooking(
            @PathVariable Long id,
            @Valid @RequestBody PayBookingRequest request) {

        return ResponseEntity.ok(bookingService.payBooking(id, request));
    }*/
    // CANCEL
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels a booking and applies cancellation rules.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking canceled",
                content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Cancellation not allowed",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {

        return ResponseEntity.ok(bookingService.cancelBooking(id, request));
    }

    // EXPIRE PENDING BOOKINGS (manual trigger)
    @PostMapping("/expire-pending")
    @Operation(summary = "Expire pending bookings", description = "Triggers expiration of overdue pending bookings.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending bookings expired",
                content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> expirePendingBookings() {

        bookingService.expireOverduePendingBookings();

        return ResponseEntity.ok(Map.of(
                "message", "Expired pending bookings processed"
        ));
    }

    // FILTER
    /*@GetMapping("/filter")
    @Operation(summary = "Filter bookings", description = "Searches bookings using status, date, guest, room and amount filters.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bookings returned",
                content = @Content(schema = @Schema(implementation = BookingSummary.class)))
    })
    public ResponseEntity<List<BookingSummary>> filterBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long guestId,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {

        BookingFilterRequest filter = new BookingFilterRequest();
        filter.setStatus(status);
        filter.setGuestId(guestId);
        filter.setHotelId(hotelId);
        filter.setRoomId(roomId);
        filter.setCheckInFrom(checkInFrom);
        filter.setCheckInTo(checkInTo);
        filter.setCheckOutFrom(checkOutFrom);
        filter.setCheckOutTo(checkOutTo);
        filter.setMinAmount(minAmount);
        filter.setMaxAmount(maxAmount);

        return ResponseEntity.ok(bookingService.filterBookings(filter));
    }*/
    // AVAILABILITY
    @GetMapping("/availability")
    @Operation(summary = "Check room availability", description = "Checks if a room is available in a date range and optionally returns quote.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability returned",
                content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam @NotNull Long roomId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime checkInTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime checkOutTime,
            @RequestParam(defaultValue = "1") int guests) {

        boolean available = bookingService.isRoomAvailable(roomId, checkIn, checkOut, checkInTime, checkOutTime);

        if (available) {
            BigDecimal quote = bookingService.getQuote(roomId, checkIn, checkOut, checkInTime, checkOutTime);
            return ResponseEntity.ok(Map.of(
                    "roomId", roomId,
                    "available", true,
                    "checkIn", checkIn,
                    "checkOut", checkOut,
                    "checkInTime", checkInTime,
                    "checkOutTime", checkOutTime,
                    "quote", quote
            ));
        }

        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "available", false,
                "checkIn", checkIn,
                "checkOut", checkOut,
                "checkInTime", checkInTime,
                "checkOutTime", checkOutTime
        ));
    }

    // PRICE QUOTE
    @GetMapping("/quote")
    @Operation(summary = "Get price quote", description = "Calculates booking price quote for room and date range.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quote returned",
                content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Map<String, Object>> getQuote(
            @RequestParam @NotNull Long roomId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime checkInTime,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime checkOutTime) {

        BigDecimal quote = bookingService.getQuote(roomId, checkIn, checkOut, checkInTime, checkOutTime);
        long nights = checkIn.until(checkOut).getDays();

        return ResponseEntity.ok(Map.of(
                "roomId", roomId,
                "checkIn", checkIn,
                "checkOut", checkOut,
                "nights", nights,
                "total", quote
        ));
    }

    // GUEST HISTORY
    /*@GetMapping("/guest/{guestId}")
    @Operation(summary = "Get guest booking history", description = "Returns booking history for a guest.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guest history returned",
                content = @Content(schema = @Schema(implementation = BookingSummary.class)))
    })
    public ResponseEntity<List<BookingSummary>> getGuestHistory(
            @PathVariable Long guestId) {

        return ResponseEntity.ok(bookingService.getGuestBookingHistory(guestId));
    }*/
    // UPCOMING
    /*@GetMapping("/upcoming")
    @Operation(summary = "Get upcoming bookings", description = "Returns upcoming bookings.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Upcoming bookings returned",
                content = @Content(schema = @Schema(implementation = BookingSummary.class)))
    })
    public ResponseEntity<List<BookingSummary>> getUpcomingBookings() {
        return ResponseEntity.ok(bookingService.getUpcomingBookings());
    }*/
    // MY BOOKINGS          
    @GetMapping("/my")
    @Operation(summary = "Get my bookings", description = "Returns booking history for the authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "My bookings returned",
                content = @Content(schema = @Schema(implementation = BookingSummary.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BookingSummary>> getMyBookings(
            @SortDefault(sort = "checkInDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(pageable, hotelName, status, checkInFrom, checkInTo, checkOutFrom, checkOutTo));
    }

    @PreAuthorize("@bookingSecurity.canViewBooking(authentication, #hotelId) ") // Only users with this permission or ADMIN role can view hotel bookings
    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel bookings", description = "Returns booking history for a specific hotel.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hotel bookings returned",
                content = @Content(schema = @Schema(implementation = BookingSummary.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<List<BookingSummary>> getBookingsForHotel(
            @PathVariable Long hotelId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo
    ) {
        return ResponseEntity.ok(bookingService.getBookingsForHotel(hotelId, roomNumber, guestName, status, checkInFrom, checkInTo, checkOutFrom, checkOutTo));
    }

}
