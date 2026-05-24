package com.example.demo.Booking.filterBooking;

import com.example.demo.Booking.entityBooking.Booking;

import com.example.demo.Booking.entityBooking.BookingStatus;

import com.example.demo.Booking.servicesBooking.BookingService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder;    


import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate; 
import java.math.BigDecimal;
import com.example.demo.payment.PaymentStatus;

import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.Room;

public class BookingSpec {

    /**
     * Builds a dynamic JPA Specification from a BookingFilterRequest.
     * Only non-null fields are added as predicates.
     */
    public static Specification<Booking> withFilters(BookingFilterRequest filter) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // status = ?
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // guest.id = ?
            if (filter.getGuestId() != null) {
                predicates.add(cb.equal(root.get("guest").get("id"), filter.getGuestId()));
            }

            // hotel.id = ?
            if (filter.getHotelId() != null) {
                predicates.add(cb.equal(root.get("hotel").get("id"), filter.getHotelId()));
            }

            // room.id = ?
            if (filter.getRoomId() != null) {
                predicates.add(cb.equal(root.get("room").get("id"), filter.getRoomId()));
            }

            // checkInDate >= checkInFrom
            if (filter.getCheckInFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), filter.getCheckInFrom()));
            }

            // checkInDate <= checkInTo
            if (filter.getCheckInTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInDate"), filter.getCheckInTo()));
            }

            // checkOutDate >= checkOutFrom
            if (filter.getCheckOutFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkOutDate"), filter.getCheckOutFrom()));
            }

            // checkOutDate <= checkOutTo
            if (filter.getCheckOutTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), filter.getCheckOutTo()));
            }

            // totalAmount >= minAmount
            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinAmount()));
            }

            // totalAmount <= maxAmount
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxAmount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

 public static Specification<Booking> byUser(Long userId) {
        return (root, query, cb) -> {

            if (userId == null) {
                return cb.conjunction(); // no filter
            }

            return cb.equal(
                    root.get("guest").get("user").get("id"),
                    userId
            );
        };
 
}

public static Specification<Booking> byHotel(Long hotelId) {
        return (root, query, cb) -> {

            if (hotelId == null) {
                return cb.conjunction(); // no filter
            }

            return cb.equal(root.get("hotel").get("id"), hotelId);
        };
    }

    public static Specification<Booking> byRoom(String roomNumber) {
        return (root, query, cb) -> {

            if (roomNumber == null) {
                return cb.conjunction(); // no filter
            }

            return cb.equal(root.get("room").get("roomNumber"), roomNumber);
        };
    }


public static Specification<Booking> hasHotelName(String hotelName) {
        return (root, query, cb) -> {

            if (hotelName == null || hotelName.isBlank()) {
                return cb.conjunction(); // no filter
            }

            return cb.like(
                    cb.lower(root.get("hotel").get("name")),
                    "%" + hotelName.toLowerCase() + "%"
            );
        };
    }


    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction(); // no filter
            }

            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Booking> checkInBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInDate"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Booking> checkOutBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkOutDate"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkOutDate"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }



}
