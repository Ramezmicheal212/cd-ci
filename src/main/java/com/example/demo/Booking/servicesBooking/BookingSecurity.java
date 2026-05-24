package com.example.demo.Booking.servicesBooking;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.example.demo.auth.repository.UserHotelRepository;
import com.example.demo.auth.entity.EmploymentStatus;

import lombok.RequiredArgsConstructor;


@Component("bookingSecurity")
@RequiredArgsConstructor
public class BookingSecurity {
    // This class can be used
    // to centralize any booking-related security checks in the future.
    // For now, we are using method-level security annotations directly in the controllers and services.
    private final UserHotelRepository userHotelRepository;

    public boolean hasBookingPermission(Authentication authentication, Long hotelId, String permissionName) {
        if (authentication == null || hotelId == null || permissionName == null || permissionName.isBlank()) {
            return false;
        }
         String username = authentication.getName();

         if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
      return userHotelRepository.existsByUserUsernameAndHotelIdAndHotelRolePermissionsNameAndStatus(username, hotelId, permissionName, EmploymentStatus.ACTIVE);
}


    public boolean canViewBooking(Authentication authentication, Long hotelId) {
        return hasBookingPermission(authentication, hotelId, "VIEW_BOOKINGS_OF_HOTEL");
    }
}