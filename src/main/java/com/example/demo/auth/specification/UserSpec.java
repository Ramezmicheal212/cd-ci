package com.example.demo.auth.specification;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.auth.entity.User;

public class UserSpec {
    public static Specification<User> startsWith(String username) {
        return (root, query, criteriaBuilder) -> {
            if (username == null || username.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.like(root.get("username"), username + "%");
        };
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            if (roleName == null || roleName.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.equal(root.join("roles").get("name"), roleName);
        };
    }

    public static Specification<User> hasHotel(String hotelName) {
        return (root, query, criteriaBuilder) -> {
            if (hotelName == null || hotelName.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.equal(root.join("userHotels").get("hotel").get("name"), hotelName);
        };
    }
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.like(root.get("email"), email + "%");
        };
    }

    public static Specification<User> hasPhoneNumber(String phoneNumber) {
        return (root, query, criteriaBuilder) -> {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.like(root.get("phoneNumber"), phoneNumber + "%");
        };
    }
    public static Specification<User> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }
    public static Specification<User> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.like(root.get("fullName"), fullName + "%");
        };
    }
    
}
