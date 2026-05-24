package com.example.demo.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.example.demo.auth.entity.*;

import com.example.demo.auth.repository.UserHotelRepository;
import com.example.demo.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Component("hotelSecurity")
@RequiredArgsConstructor
public class HotelSecurity {
    private final UserRepository userRepository;
    private final UserHotelRepository userHotelRepository;

    public boolean isAuthorizedForHotel(Authentication authentication, Long hotelId) {
        if (authentication == null || hotelId == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            List<Long> jwtHotelsId = jwt.getClaim("hotelIds");
            return jwtHotelsId != null && jwtHotelsId.contains(hotelId);
        }
        return false;
    }

    public boolean hasHotelPermission(Authentication authentication, Long hotelId, String permissionName) {
        if (authentication == null || hotelId == null || permissionName == null || permissionName.isBlank()) {
            return false;
        }

          
         String username = authentication.getName();
         //if the user is admin
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return true;
            }
         System.out.println("Checking permission for user: " + username + ", hotelId: " + hotelId + ", permission: " + permissionName);
        return userHotelRepository.existsByUserUsernameAndHotelIdAndHotelRolePermissionsNameAndStatus(username, hotelId, permissionName, EmploymentStatus.ACTIVE);
    }

    public boolean canUpdateHotel(Authentication authentication, Long hotelId) {
        return hasHotelPermission(authentication, hotelId, "HOTEL_UPDATE");
    }
    public boolean canDeleteHotel(Authentication authentication, Long hotelId) {
        return hasHotelPermission(authentication, hotelId, "HOTEL_DELETE");
    }
    public boolean canViewHotelManager(Authentication authentication, Long hotelId) {
        return hasHotelPermission(authentication, hotelId, "HOTEL_VIEW_MANAGER");
    }
    public boolean canManageHotelEmployees(Authentication authentication, Long hotelId) {
        return hasHotelPermission(authentication, hotelId, "ADD_EMPLOYEE") || hasHotelPermission(authentication, hotelId, "REMOVE_EMPLOYEE");
    }

 public boolean canViewHotelEmployees(Authentication authentication, Long hotelId) {
        return hasHotelPermission(authentication, hotelId, "HOTEL_VIEW_EMPLOYEES") ;
    }

    
    
}
