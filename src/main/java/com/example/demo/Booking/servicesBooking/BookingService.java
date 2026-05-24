package com.example.demo.Booking.servicesBooking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Booking.bookingMapper.BookingMapper;
import com.example.demo.Booking.bookingRepo.BookingRepository;
import com.example.demo.Booking.dtoBooking.Booking.BookingResponse;
import com.example.demo.Booking.dtoBooking.Booking.BookingSummary;
import com.example.demo.Booking.dtoBooking.Booking.CancelBookingRequest;
import com.example.demo.Booking.dtoBooking.Booking.CreateBookingRequest;
import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.Booking.entityBooking.BookingStatus;
import com.example.demo.Booking.filterBooking.BookingFilterRequest;
import com.example.demo.Booking.filterBooking.BookingSpec;
import com.example.demo.Booking.servicesBooking.CancellationPolicyService.PolicyResult;
import com.example.demo.ExceptionHandler.BookingException;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import com.example.demo.auth.notification.EmailService;
import com.example.demo.catalog.entity.Guest;
import com.example.demo.catalog.entity.Room;
import com.example.demo.catalog.repository.GuestRepository;
import com.example.demo.catalog.repository.RoomRepository;
import com.example.demo.payment.Payment;
import com.example.demo.payment.PaymentMethod;
import com.example.demo.payment.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final CancellationPolicyService cancellationPolicyService;
    private final BookingMapper bookingMapper;
    private final BookingPricingFacade bookingPricingFacade;
    private final EmailService emailService;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE BOOKING
    // ─────────────────────────────────────────────────────────────────────────
   @Transactional
public BookingResponse createBooking(CreateBookingRequest request) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
        throw new IllegalStateException("No authenticated user found");
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();
    long userId = jwt.getClaim("userId");

    log.info("Creating booking for guest={} room={}", request.getGuestId(), request.getRoomId());

    // ================= Guest =================
    Guest guest = guestRepository.findByIdAndUserId(request.getGuestId(), userId)
            .orElseThrow(() -> new AccessDeniedException("This guest does not belong to you"));

    // ================= Room =================
    Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

    if (Boolean.FALSE.equals(room.getIsActive()) || Boolean.FALSE.equals(room.getHotel().getIsActive())) {
        throw new BookingException("Room or hotel is archived");
    }

    // ================= Validation =================
    validateDates(request.getCheckInDate(), request.getCheckOutDate());

    if (request.getNumberOfGuests() > room.getCapacity()) {
        throw new BookingException("Room capacity exceeded");
    }

   



    // ================= Availability =================
    boolean alreadyBooked = bookingRepository.existsOverlappingBooking(
            room.getId(),
            request.getCheckInDate(),
            request.getCheckOutDate(),
            BookingStatus.CANCELLED
    );

    if (alreadyBooked) {
        throw new BookingException("Room is not available for selected dates");
    }

    // ================= Pricing =================
    BigDecimal total = bookingPricingFacade.calculateFinalPrice(
            room.getBasePrice(),
             request.getCheckInDate(),
             request.getCheckOutDate(),
            request.getCouponCode()
    );

    BigDecimal minimumPayment = total.multiply(BigDecimal.valueOf(0.5))
            .setScale(2, RoundingMode.HALF_UP);

    // ================= Booking Creation =================
    Booking booking = Booking.builder()
            .guest(guest)
            .hotel(room.getHotel())
            .room(room)
            .numberOfGuests(request.getNumberOfGuests())
            .checkInDate(request.getCheckInDate())
            .checkOutDate(request.getCheckOutDate())
            .totalAmount(total)
            .minimumRequiredPayment(minimumPayment)
            .build();

    booking = bookingRepository.save(booking);
     
    log.info("Booking {} created successfully", booking.getId());
   
            
    emailService.sendEmail(
            guest.getUser().getEmail(),
            "Booking Created",
            "Your booking has been created successfully. Booking ID: " + booking.getId()
    );

    return bookingMapper.toResponse(booking);
}

   
    // ─────────────────────────────────────────────────────────────────────────
    // CANCEL BOOKING
    // ─────────────────────────────────────────────────────────────────────────
   @Transactional
public BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request) {

    Booking booking = getBookingEntity(bookingId);

    if (booking.getStatus() == BookingStatus.CANCELLED) {
        throw new BookingException("Already cancelled");
    }

    PolicyResult policy = cancellationPolicyService.evaluate(
            booking.getCheckInDate()
    );

    String reason = (request != null && request.getReason() != null)
            ? request.getReason()
            : "Cancelled by guest";

    // ================= Refund Calculation =================
    BigDecimal totalPaid = booking.getTotalPaid();

    BigDecimal refundAmount = totalPaid
            .multiply(BigDecimal.valueOf(policy.refundPercentage() / 100.0))
            .setScale(2, RoundingMode.HALF_UP);

    // ================= Create REFUND Payment =================
    if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {

        Payment refund = Payment.builder()
                .booking(booking)
                .amount(refundAmount)
                .status(PaymentStatus.REFUNDED)
                .paymentMethod(PaymentMethod.BANK_TRANSFER) // أو SYSTEM
                .paidAt(LocalDateTime.now())
                .build();

        booking.getPayments().add(refund);
    }

    // ================= Cancel Booking =================
    booking.setStatus(BookingStatus.CANCELLED);
    booking.setCancellationReason(reason);

    booking = bookingRepository.save(booking);

    log.info("Booking {} cancelled with refund {}%", bookingId, policy.refundPercentage());

    return bookingMapper.toResponse(booking);
}

    // ─────────────────────────────────────────────────────────────────────────
    // SCHEDULED EXPIRY
    // ─────────────────────────────────────────────────────────────────────────
   @Scheduled(cron = "0 */1 * * * *")
@Transactional
public void expireOverduePendingBookings() {

    LocalDateTime now = LocalDateTime.now();

    List<Booking> expired = bookingRepository
            .findExpiredPendingBookings(now, BookingStatus.PENDING);

    if (expired.isEmpty()) {
        return;
    }

    expired.forEach(booking ->
            cancelBookingInternal(
                    booking,
                    "Automatically cancelled: payment deadline passed"
            )
    );

    log.info("Expired {} bookings", expired.size());

    // No need for saveAll if using @Transactional (dirty checking)
}
    // ─────────────────────────────────────────────────────────────────────────
    // FILTER
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
public Page<BookingSummary> filterBookings(
        BookingFilterRequest filter,
        Pageable pageable
) {

    return bookingRepository
            .findAll(BookingSpec.withFilters(filter), pageable)
            .map(bookingMapper::toSummary);
}

    // ─────────────────────────────────────────────────────────────────────────
    // AVAILABILITY
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, LocalTime checkInTime, LocalTime checkOutTime) {
        validateDates(checkIn, checkOut);



        return !bookingRepository.existsOverlappingBooking(
                roomId,
                checkIn,
                checkOut,
                BookingStatus.CANCELLED
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUOTE
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public BigDecimal getQuote(Long roomId, LocalDate checkIn, LocalDate checkOut, LocalTime checkInTime, LocalTime checkOutTime) {

        validateDates(checkIn, checkOut);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (Boolean.FALSE.equals(room.getIsActive()) || Boolean.FALSE.equals(room.getHotel().getIsActive())) {
            throw new BookingException("Room or hotel is archived");
        }

        LocalDateTime checkInTimeDate = LocalDateTime.of(
                checkIn,
                checkInTime
        );
        LocalDateTime checkOutTimeDate = LocalDateTime.of(
                checkOut,
                checkOutTime
        );

       

        return bookingPricingFacade.calculateFinalPrice(
                room.getBasePrice(),
                checkIn,
                checkOut,
                null
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORY
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BookingSummary> getGuestBookingHistory(Long guestId) {

        if (!guestRepository.existsById(guestId)) {
            throw new ResourceNotFoundException("Guest not found");
        }

        return bookingRepository.findByGuestIdOrderByCreatedAtDesc(guestId)
                .stream()
                .map(bookingMapper::toSummary)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPCOMING
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<BookingSummary> getUpcomingBookings() {
        return bookingRepository.findUpcomingBookings(LocalDate.now(), BookingStatus.CANCELLED)
                .stream()
                .map(bookingMapper::toSummary)
                .toList();
    }

        public List<BookingSummary> getMyBookings(Pageable pageable, String hotelName,  BookingStatus status , LocalDate checkInFrom , LocalDate checkInTo , LocalDate checkOutFrom , LocalDate checkOutTo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Long userId = jwt.getClaim("userId");

    Specification<Booking> spec = Specification
            .where(BookingSpec.byUser(userId))
            .and(BookingSpec.hasStatus(status))
            .and(BookingSpec.hasHotelName(hotelName))
            .and(BookingSpec.checkInBetween(checkInFrom, checkInTo))
            .and(BookingSpec.checkOutBetween(checkOutFrom, checkOutTo));

    return bookingRepository.findAll(spec, pageable)
            .stream()
            .map(bookingMapper::toSummary)
            .toList();
        
        
    }

    //get bokings for a specific hotel
    public List<BookingSummary> getBookingsForHotel(Long hotelId , String roomNumber , String guestName , BookingStatus status , LocalDate checkInFrom , LocalDate checkInTo , LocalDate checkOutFrom , LocalDate checkOutTo) {
        Specification<Booking> spec = Specification
                .where(BookingSpec.byHotel(hotelId))
                .and(BookingSpec.byRoom(roomNumber))
                .and(BookingSpec.hasStatus(status))
                .and(BookingSpec.checkInBetween(checkInFrom, checkInTo))
                .and(BookingSpec.checkOutBetween(checkOutFrom, checkOutTo));

        return bookingRepository.findAll(spec)
                .stream()
                .map(bookingMapper::toSummary)
                .toList();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // SINGLE BOOKING
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        return bookingMapper.toResponse(getBookingEntity(bookingId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void cancelBookingInternal(Booking booking, String reason) {
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
    }

    private void saveCancelled(Booking booking, String reason) {
        cancelBookingInternal(booking, reason);
        bookingRepository.save(booking);
    }

    private boolean isPaymentExpired(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING
                && LocalDateTime.now().isAfter(booking.getPaymentDeadline());
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BookingException("Dates must not be null");
        }

        if (!checkIn.isBefore(checkOut)) {
            throw new BookingException("Invalid dates");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new BookingException("Check-in must be in the future");
        }
    }




}

