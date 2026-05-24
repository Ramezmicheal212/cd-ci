package com.example.demo.catalog.Specification;

import com.example.demo.catalog.entity.Amenity;
import com.example.demo.catalog.entity.Room;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.persistence.criteria.Join;

public class RoomSpec {

    public static Specification<Room> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

    public static Specification<Room> belongsToHotel(Long hotelId) {
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("hotel").get("id"), hotelId);
        };
    }

    public static Specification<Room> hasCapacity(Integer capacity) {
        return (root, query, criteriaBuilder) -> {
            if (capacity == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("capacity"), capacity);
        };
    }

    public static Specification<Room> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            } else if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("basePrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
            }
        };
    }

    public static Specification<Room> hasRoomType(String roomType) {
        return (root, query, criteriaBuilder) -> {
            if (roomType == null || roomType.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("roomType"), roomType);
        };
    }

    public static Specification<Room> hasAmenities(Set<String> amenities) {
        return (root, query, criteriaBuilder) -> {
            if (amenities == null || amenities.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Room, Amenity> amenityJoin = root.join("amenities");
            return amenityJoin.get("name").in(amenities);
        };
    }

}
