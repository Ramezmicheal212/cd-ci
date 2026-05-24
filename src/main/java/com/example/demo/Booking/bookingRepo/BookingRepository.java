package com.example.demo.Booking.bookingRepo;

import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.Booking.entityBooking.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ Repository 

    public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

        boolean existsByHotelId(Long hotelId);

        boolean existsByRoomId(Long roomId);

      
        // Full booking history (newest first)
        List<Booking> findByGuestIdOrderByCreatedAtDesc(Long guestId);

 
        // Upcoming bookings (not cancelled)
        @Query("""
        SELECT b FROM Booking b
        WHERE b.checkInDate >= :today
          AND b.status <> :cancelledStatus
        ORDER BY b.checkInDate ASC
    """)
        List<Booking> findUpcomingBookings(
                @Param("today") LocalDate today,
                @Param("cancelledStatus") BookingStatus cancelledStatus
        );

        // ================= Availability =================
        @Query("""
                                SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
                                FROM Booking b
                                WHERE b.room.id = :roomId
                                        AND b.status <> :finishedStatus
                """)
        boolean existsActiveBookingsByRoomId(
                @Param("roomId") Long roomId,
                @Param("finishedStatus") BookingStatus finishedStatus
        );

        @Query("""
                                SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
                                FROM Booking b
                                WHERE b.room.hotel.id = :hotelId
                                        AND b.status <> :finishedStatus
                """)
        boolean existsActiveBookingsByHotelId(
                @Param("hotelId") Long hotelId,
                @Param("finishedStatus") BookingStatus finishedStatus
        );

        // Get overlapping bookings (for validation/debugging)
        @Query("""
        SELECT b FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status <> :cancelledStatus
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
        List<Booking> findOverlappingByRoom(
                @Param("roomId") Long roomId,
                @Param("checkIn") LocalDate checkIn,
                @Param("checkOut") LocalDate checkOut,
                @Param("cancelledStatus") BookingStatus cancelledStatus
        );

        // Fast boolean check (BEST for performance)
        @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status <> :cancelledStatus
          AND b.checkInDate < :checkOut
          AND b.checkOutDate > :checkIn
    """)
        boolean existsOverlappingBooking(
                @Param("roomId") Long roomId,
                @Param("checkIn") LocalDate checkIn,
                @Param("checkOut") LocalDate checkOut,
                @Param("cancelledStatus") BookingStatus cancelledStatus
        );

        // ================= Filters =================
        List<Booking> findByStatusOrderByCheckInDateAsc(BookingStatus status);

        // ================= Payment / Expiry =================
        @Query("""
        SELECT b FROM Booking b
        WHERE b.status = :pendingStatus
          AND b.paymentDeadline < :now
    """)
        List<Booking> findExpiredPendingBookings(
                @Param("now") LocalDateTime now,
                @Param("pendingStatus") BookingStatus pendingStatus
        );

        List<Booking> findByGuestUserIdOrderByCreatedAtDesc(Long userId);

        List<Booking> findByHotelIdOrderByCreatedAtDesc(Long hotelId);

    }

    
    
